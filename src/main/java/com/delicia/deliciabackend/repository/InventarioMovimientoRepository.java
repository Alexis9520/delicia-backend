package com.delicia.deliciabackend.repository;

import com.delicia.deliciabackend.model.InventarioMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {

    @EntityGraph(attributePaths = {"lote"})
    Page<InventarioMovimiento> findByProductoId(Long productoId, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Page<InventarioMovimiento> findAllByLote_Id(Long loteId, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Page<InventarioMovimiento> findAll(Pageable pageable);

    // Búsqueda por código del lote (string) — exacta
    @EntityGraph(attributePaths = {"lote"})
    Page<InventarioMovimiento> findByLote_Codigo(String codigo, Pageable pageable);

    // Búsqueda por código del lote ignorando mayúsculas/minúsculas
    @EntityGraph(attributePaths = {"lote"})
    Page<InventarioMovimiento> findByLote_CodigoIgnoreCase(String codigo, Pageable pageable);

    // Slice variants (evitan COUNT) si las usas:
    @EntityGraph(attributePaths = {"lote"})
    Slice<InventarioMovimiento> findSliceByProductoId(Long productoId, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Slice<InventarioMovimiento> findSliceByLote_Id(Long loteId, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Slice<InventarioMovimiento> findSliceByLote_Codigo(String codigo, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Slice<InventarioMovimiento> findSliceByLote_CodigoIgnoreCase(String codigo, Pageable pageable);

    @EntityGraph(attributePaths = {"lote"})
    Slice<InventarioMovimiento> findAllBy(Pageable pageable);
}