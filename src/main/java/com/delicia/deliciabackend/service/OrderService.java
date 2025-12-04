package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.OrderRequest;
import com.delicia.deliciabackend.dto.OrderItemRequest;
import com.delicia.deliciabackend.dto.AddressRequest;
import com.delicia.deliciabackend.model.*;
import com.delicia.deliciabackend.repository.OrderRepository;
import com.delicia.deliciabackend.repository.ProductRepository;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import com.delicia.deliciabackend.repository.InventarioMovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    // Crear pedido normal (cliente online)
    @Transactional
    public Order create(OrderRequest req, String userEmail) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));

        Order order = new Order();
        order.setPaymentMethod(req.getPaymentMethod());
        order.setPaymentIntentId(req.getPaymentIntentId());
        order.setUsuario(usuario); // Asociar el usuario
        order.setStatus("pendiente");
        order.setCreatedAt(new Date());
        order.setCanal(req.getCanal() != null ? req.getCanal() : "online");

        // Dirección
        AddressRequest addrReq = req.getAddress();
        Address address = new Address();
        address.setStreet(addrReq.getStreet());
        address.setCity(addrReq.getCity());
        address.setPostalCode(addrReq.getPostalCode());
        address.setCountry(addrReq.getCountry());
        address.setPhone(addrReq.getPhone());
        order.setAddress(address);

        // Items y cálculo de total
        double total = 0.0;
        List<OrderItem> items = new ArrayList<>();
        List<OrderItemRecord> movimientosPendientes = new ArrayList<>();

        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("El id de producto no puede ser null.");
            }
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getProductId()));

            // Reducir stock inmediatamente (si falla, aborta creación del pedido)
            productService.reducirStock(product.getId(), itemReq.getQuantity());

            OrderItem item = new OrderItem();
            item.setQuantity(itemReq.getQuantity());
            item.setProduct(product);
            item.setOrder(order);
            items.add(item);
            total += product.getPrice() * itemReq.getQuantity();

            movimientosPendientes.add(new OrderItemRecord(product.getId(), itemReq.getQuantity()));
        }
        order.setItems(items);
        order.setTotal(total);

        // Persistir pedido
        Order saved = orderRepository.save(order);

        // Registrar movimientos de inventario tipo SALIDA (VENTA) con referencia al id del pedido
        for (OrderItemRecord r : movimientosPendientes) {
            InventarioMovimiento mov = new InventarioMovimiento();
            mov.setProductoId(r.productoId);
            mov.setCantidad(-Math.abs(r.cantidad)); // negativo para salida
            mov.setTipo(TipoMovimiento.SALIDA);
            mov.setMotivo(MotivoMovimiento.VENTA);
            // Referencia flexible: PEDIDO / <id>
            mov.setReferenciaTipo("PEDIDO");
            mov.setReferencia(String.valueOf(saved.getId()));
            mov.setCreatedAt(new Date());
            inventarioMovimientoRepository.save(mov);
        }

        return saved;
    }

    // Registrar venta mostrador (asociar a usuario autenticado si existe)
    @Transactional
    public Order createMostrador(OrderRequest req) {
        return createMostrador(req, null);
    }

    @Transactional
    public Order createMostrador(OrderRequest req, String userEmail) {
        Order order = new Order();
        order.setPaymentMethod(req.getPaymentMethod());
        order.setPaymentIntentId(req.getPaymentIntentId());
        order.setStatus("entregado"); // Directo a entregado
        order.setCreatedAt(new Date());
        order.setCanal(req.getCanal() != null ? req.getCanal() : "mostrador");
        order.setNombreCliente(req.getNombreCliente());
        order.setDocumentoCliente(req.getDocumentoCliente());

        // Dirección para mostrador (puede ser null o fijo "Mostrador")
        Address address = new Address();
        address.setStreet("Mostrador");
        address.setCity("");
        address.setPostalCode("");
        address.setCountry("");
        address.setPhone("");
        order.setAddress(address);

        // Items y cálculo de total
        double total = 0.0;
        List<OrderItem> items = new ArrayList<>();
        List<OrderItemRecord> movimientosPendientes = new ArrayList<>();

        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("El id de producto no puede ser null.");
            }
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getProductId()));

            // Reducir stock
            productService.reducirStock(product.getId(), itemReq.getQuantity());

            OrderItem item = new OrderItem();
            item.setQuantity(itemReq.getQuantity());
            item.setProduct(product);
            item.setOrder(order);
            items.add(item);
            total += product.getPrice() * itemReq.getQuantity();

            movimientosPendientes.add(new OrderItemRecord(product.getId(), itemReq.getQuantity()));
        }
        order.setItems(items);
        order.setTotal(total);

        // If userEmail provided, associate usuario
        if (userEmail != null) {
            usuarioRepository.findByEmail(userEmail).ifPresent(order::setUsuario);
        }

        Order saved = orderRepository.save(order);

        // Registrar movimientos SALIDA/Venta
        for (OrderItemRecord r : movimientosPendientes) {
            InventarioMovimiento mov = new InventarioMovimiento();
            mov.setProductoId(r.productoId);
            mov.setCantidad(-Math.abs(r.cantidad)); 
            mov.setTipo(TipoMovimiento.SALIDA);
            mov.setMotivo(MotivoMovimiento.VENTA);
            mov.setReferenciaTipo("PEDIDO");
            mov.setReferencia(String.valueOf(saved.getId()));
            mov.setCreatedAt(new Date());
            inventarioMovimientoRepository.save(mov);
        }

        return saved;
    }

    // ========================================================
    // Métodos públicos que usan los controladores (añadidos)
    // ========================================================

    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> findByUsuarioEmail(String email) {
        return orderRepository.findByUsuarioEmail(email);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findByCanal(String canal) {
        return orderRepository.findByCanal(canal);
    }

    // Clase interna para portar pares productoId-cantidad
    private static class OrderItemRecord {
        Long productoId;
        int cantidad;
        OrderItemRecord(Long productoId, int cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }
    }
}