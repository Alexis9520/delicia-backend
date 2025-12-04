package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.InventarioMovimientoDto;
import com.delicia.deliciabackend.dto.PaginatedResponse;
import com.delicia.deliciabackend.model.InventarioMovimiento;
import com.delicia.deliciabackend.repository.InventarioMovimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.delicia.deliciabackend.dto.ProduccionItemRequest;
import com.delicia.deliciabackend.service.InventarioService;

import java.util.List;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private static final Logger log = LoggerFactory.getLogger(InventarioController.class);

    @Autowired
    private InventarioMovimientoRepository movimientoRepository;

    @Autowired
    private InventarioService inventarioService;

    /**
     * Endpoint genérico: /api/inventario/movimientos?lote=... (acepta id o código)
     * Ya existente en versiones anteriores.
     */
    @GetMapping("/movimientos")
    public ResponseEntity<PaginatedResponse<InventarioMovimientoDto>> getMovimientos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false, name = "lote") String loteParam // puede ser id o codigo o "LOTE: ..."
    ) {
        int p = Math.max(1, page);
        int ps = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(p - 1, ps, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<InventarioMovimiento> movimientosPage;

        try {
            if (productoId != null) {
                log.info("Buscando movimientos por productoId={}", productoId);
                movimientosPage = movimientoRepository.findByProductoId(productoId, pageable);
            } else if (loteParam != null && !loteParam.isBlank()) {
                String original = loteParam;
                String normalized = loteParam.replaceFirst("(?i)^\\s*LOTE\\s*:?", "").trim();
                log.info("Parámetro lote recibido='{}', normalizado a='{}'", original, normalized);

                try {
                    Long loteId = Long.parseLong(normalized);
                    log.info("Interpretado como loteId numérico: {}", loteId);
                    movimientosPage = movimientoRepository.findAllByLote_Id(loteId, pageable);
                } catch (NumberFormatException nfe) {
                    log.info("Buscando movimientos por lote.codigo='{}' (exacto)", normalized);
                    movimientosPage = movimientoRepository.findByLote_Codigo(normalized, pageable);
                    if (movimientosPage == null || movimientosPage.isEmpty()) {
                        log.info("No encontrados por exact match, intentando ignore-case para='{}'", normalized);
                        movimientosPage = movimientoRepository.findByLote_CodigoIgnoreCase(normalized, pageable);
                    }
                }
            } else {
                log.info("Buscando todos los movimientos (sin filtro)");
                movimientosPage = movimientoRepository.findAll(pageable);
            }
        } catch (Exception ex) {
            log.error("Error buscando movimientos con loteParam='{}' productoId='{}': {}", loteParam, productoId, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(null);
        }

        List<InventarioMovimientoDto> contentDto = movimientosPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginatedResponse<InventarioMovimientoDto> resp = new PaginatedResponse<InventarioMovimientoDto>(
                contentDto,
                movimientosPage.getTotalElements(),
                p,
                ps,
                movimientosPage.getTotalPages(),
                movimientosPage.hasNext()
        );

        log.info("Movimientos encontrados: {} (page={}, pageSize={})", movimientosPage.getNumberOfElements(), p, ps);
        return ResponseEntity.ok(resp);
    }

    /**
     * Nuevo endpoint: /api/inventario/lotes/{codigo}/movimientos
     * Este coincide con la ruta que tu frontend llama desde LoteDetailPage.
     * Acepta {codigo} como código de lote (p.ej. "L-20251118103333-2").
     */
    @GetMapping("/lotes/{codigo}/movimientos")
    public ResponseEntity<PaginatedResponse<InventarioMovimientoDto>> getMovimientosByLoteCodigo(
            @PathVariable("codigo") String codigo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "500") int pageSize // pageSize grande por defecto, como tu UI usaba
    ) {
        int p = Math.max(1, page);
        int ps = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(p - 1, ps, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<InventarioMovimiento> movimientosPage;
        try {
            String normalized = codigo.replaceFirst("(?i)^\\s*LOTE\\s*:?", "").trim();
            log.info("GET /lotes/{codigo}/movimientos - codigo recibido='{}' normalizado='{}'", codigo, normalized);

            // Intentamos búsqueda por código ignore-case (repo debe tener findByLote_CodigoIgnoreCase)
            movimientosPage = movimientoRepository.findByLote_CodigoIgnoreCase(normalized, pageable);
            if (movimientosPage == null) {
                movimientosPage = Page.empty(pageable);
            }
        } catch (Exception ex) {
            log.error("Error en getMovimientosByLoteCodigo codigo='{}': {}", codigo, ex.getMessage(), ex);
            return ResponseEntity.status(500).body(null);
        }

        List<InventarioMovimientoDto> contentDto = movimientosPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PaginatedResponse<InventarioMovimientoDto> resp = new PaginatedResponse<InventarioMovimientoDto>(
                contentDto,
                movimientosPage.getTotalElements(),
                p,
                ps,
                movimientosPage.getTotalPages(),
                movimientosPage.hasNext()
        );

        log.info("Movimientos por lote '{}' encontrados: {} (page={}, pageSize={})", codigo, movimientosPage.getNumberOfElements(), p, ps);
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint para registrar lote de producción desde la UI de trabajador.
     * POST /api/inventario/lote-produccion
     */
    @PreAuthorize("hasAnyRole('TRABAJADOR','ADMIN')")
    @PostMapping("/lote-produccion")
    public ResponseEntity<?> postLoteProduccion(@RequestBody List<ProduccionItemRequest> items, Principal principal) {
        try {
            String userEmail = (principal != null) ? principal.getName() : null;
            inventarioService.procesarLoteProduccion(items, userEmail);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Error procesando lote de producción: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    private InventarioMovimientoDto toDto(InventarioMovimiento m) {
        Long loteId = null;
        try {
            if (m.getLote() != null) {
                loteId = m.getLote().getId();
            }
        } catch (Exception ex) {
            log.debug("No se pudo dereferenciar lote para movimiento id={}: {}", m.getId(), ex.getMessage());
        }
        return new InventarioMovimientoDto(
                m.getId(),
                m.getProductoId(),
                m.getCantidad(),
                m.getTipo() != null ? m.getTipo().name() : null,
                m.getMotivo() != null ? m.getMotivo().name() : null,
                m.getReferenciaTipo(),
                m.getReferencia(),
                loteId,
                m.getCreatedAt()
        );
    }
}