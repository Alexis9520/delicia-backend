package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.OrderRequest;
import com.delicia.deliciabackend.dto.OrderResponse;
import com.delicia.deliciabackend.dto.StatusUpdateRequest;
import com.delicia.deliciabackend.model.Order;
import com.delicia.deliciabackend.service.OrderService;
import com.delicia.deliciabackend.dto.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    // Crear pedido y asociar al usuario logueado (cliente online)
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest req, Principal principal) {
        // Usar el email cliente@delicia.com si principal es null (demo)
        String userEmail = (principal != null) ? principal.getName() : "cliente@delicia.com";
        Order order = orderService.create(req, userEmail);
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
        String userEmail = (principal != null) ? principal.getName() : "cliente@delicia.com";
        if (order.getUsuario() == null || !order.getUsuario().getEmail().equals(userEmail)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // Consultar todos los pedidos del usuario autenticado
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrdersForUser(Principal principal) {
        String userEmail = (principal != null) ? principal.getName() : "cliente@delicia.com";
        List<Order> orders = orderService.findByUsuarioEmail(userEmail);
        List<OrderResponse> responses = orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Consultar todos los pedidos (trabajador o administrador)
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("GET /api/orders/all called");
        List<Order> orders = orderService.findAll();
        List<OrderResponse> responses = orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // Consultar pedido por ID (trabajador o administrador)
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMIN')")
    @GetMapping("/worker/{id}")
    public ResponseEntity<OrderResponse> getOrderForWorker(@PathVariable Long id) {
        log.info("GET /api/orders/worker/{} called", id);
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // Actualizar estado del pedido (trabajador o administrador)
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        order.setStatus(req.getStatus());
        orderService.save(order);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    // NUEVO: Registrar venta mostrador (trabajador o administrador)
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMIN')")
    @PostMapping("/mostrador")
    public ResponseEntity<OrderResponse> createMostrador(@RequestBody OrderRequest req, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Order order = orderService.createMostrador(req, email);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }
}