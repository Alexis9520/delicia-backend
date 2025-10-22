package com.delicia.deliciabackend.repository;

import com.delicia.deliciabackend.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    List<Comprobante> findByOrderId(Long orderId);
}