package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.OrderRequest;
import com.delicia.deliciabackend.dto.OrderResponse;
import com.delicia.deliciabackend.dto.StatusUpdateRequest;
import com.delicia.deliciabackend.model.Order;
import com.delicia.deliciabackend.service.OrderService;
import com.delicia.deliciabackend.dto.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Crear pedido y asociar al usuario logueado (cliente online)
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest req, Principal principal) {
        Order order = orderService.create(req, principal.getName());
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // Consultar pedido solo si es del usuario logueado
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, Principal principal) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (order.getUsuario() == null || !order.getUsuario().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // Consultar todos los pedidos del usuario autenticado
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersForUser(Principal principal) {
        List<Order> orders = orderService.findByUsuarioEmail(principal.getName());
        List<OrderResponse> responses = orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Consultar todos los pedidos (solo trabajador)
    @PreAuthorize("hasAuthority('ROLE_TRABAJADOR')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        List<OrderResponse> responses = orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Consultar pedido por ID (solo trabajador)
    @PreAuthorize("hasAuthority('ROLE_TRABAJADOR')")
    @GetMapping("/worker/{id}")
    public ResponseEntity<OrderResponse> getOrderForWorker(@PathVariable Long id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // Actualizar estado del pedido (solo trabajador)
    @PreAuthorize("hasAuthority('ROLE_TRABAJADOR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        order.setStatus(req.getStatus());
        orderService.save(order);
        return ResponseEntity.ok(OrderMapper.toResponse(order)); // <-- Cambiado para retornar el pedido actualizado
    }

    // NUEVO: Registrar venta mostrador (solo trabajador)
    @PreAuthorize("hasAuthority('ROLE_TRABAJADOR')")
    @PostMapping("/mostrador")
    public ResponseEntity<OrderResponse> createMostradorOrder(@RequestBody OrderRequest req) {
        Order order = orderService.createMostrador(req);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }
}