package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.OrderRequest;
import com.delicia.deliciabackend.dto.OrderItemRequest;
import com.delicia.deliciabackend.dto.AddressRequest;
import com.delicia.deliciabackend.model.*;
import com.delicia.deliciabackend.repository.OrderRepository;
import com.delicia.deliciabackend.repository.ProductRepository;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Crear pedido normal (cliente online)
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
        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("El id de producto no puede ser null.");
            }
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getProductId()));
            OrderItem item = new OrderItem();
            item.setQuantity(itemReq.getQuantity());
            item.setProduct(product);
            item.setOrder(order);
            items.add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }
        order.setItems(items);
        order.setTotal(total);

        return orderRepository.save(order);
    }

    // Registrar venta mostrador (sin usuario)
    public Order createMostrador(OrderRequest req) {
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
        for (OrderItemRequest itemReq : req.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("El id de producto no puede ser null.");
            }
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getProductId()));
            OrderItem item = new OrderItem();
            item.setQuantity(itemReq.getQuantity());
            item.setProduct(product);
            item.setOrder(order);
            items.add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }
        order.setItems(items);
        order.setTotal(total);

        return orderRepository.save(order);
    }

    // Buscar pedido por id
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Buscar todos los pedidos de un usuario por email
    public List<Order> findByUsuarioEmail(String email) {
        return orderRepository.findByUsuarioEmail(email);
    }

    // Buscar todos los pedidos (para el trabajador)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    // Guardar/actualizar pedido (para cambiar estado, etc)
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    // Buscar pedidos por canal
    public List<Order> findByCanal(String canal) {
        return orderRepository.findByCanal(canal);
    }
}