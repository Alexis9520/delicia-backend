package com.delicia.deliciabackend.repository;

import com.delicia.deliciabackend.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    Optional<Lote> findByCodigo(String codigo);
}