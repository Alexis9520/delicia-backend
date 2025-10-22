package com.delicia.deliciabackend.repository;

import com.delicia.deliciabackend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsuarioEmail(String email);

    // NUEVO: Filtrar por canal
    List<Order> findByCanal(String canal);
}