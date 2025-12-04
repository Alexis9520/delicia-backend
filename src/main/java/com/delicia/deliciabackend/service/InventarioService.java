package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.ProduccionItemRequest;
import com.delicia.deliciabackend.model.*;
import com.delicia.deliciabackend.repository.InventarioMovimientoRepository;
import com.delicia.deliciabackend.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class InventarioService {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Autowired
    private LoteRepository loteRepository;

    /**
     * Procesa lote de producción, con registro persistente de lote si no viene código.
     */
    @Transactional
    public void procesarLoteProduccion(List<ProduccionItemRequest> items, String createdBy) {
        if (items == null || items.isEmpty()) return;

        boolean anyLoteProvided = items.stream().anyMatch(i -> i.getLoteId() != null && !i.getLoteId().isBlank());

        Lote batchLote = null;
        if (!anyLoteProvided) {
            Lote nuevo = new Lote();
            nuevo.setCreatedAt(new Date());
            nuevo.setCreatedBy(createdBy);
            nuevo = loteRepository.save(nuevo);
            String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String codigo = "L-" + ts + "-" + nuevo.getId();
            nuevo.setCodigo(codigo);
            batchLote = loteRepository.save(nuevo);
        }

        int counter = 1;
        for (ProduccionItemRequest item : items) {
            productService.incrementarStock(item.getProductoId(), item.getCantidad());

            InventarioMovimiento mov = new InventarioMovimiento();
            mov.setProductoId(item.getProductoId());
            mov.setCantidad(Math.abs(item.getCantidad()));
            mov.setTipo(TipoMovimiento.ENTRADA);
            mov.setMotivo(MotivoMovimiento.PRODUCCION);

            if (item.getLoteId() != null && !item.getLoteId().isBlank()) {
                String provided = item.getLoteId().trim();
                Lote found = loteRepository.findByCodigo(provided).orElse(null);
                if (found == null) {
                    Lote newL = new Lote();
                    newL.setCodigo(provided);
                    newL.setCreatedAt(new Date());
                    newL.setCreatedBy(createdBy);
                    found = loteRepository.save(newL);
                }
                mov.setLote(found);
                mov.setReferenciaTipo("LOTE");
                mov.setReferencia(found.getCodigo());
            } else if (batchLote != null) {
                mov.setLote(batchLote);
                mov.setReferenciaTipo("LOTE");
                mov.setReferencia(batchLote.getCodigo());
            } else {
                mov.setReferenciaTipo(null);
                mov.setReferencia(null);
            }

            mov.setCreatedAt(new Date());
            inventarioMovimientoRepository.save(mov);
            counter++;
        }
    }

    /** Para compatibilidad: sigue funcionando la llamada legacy (solo items) **/
    @Transactional
    public void procesarLoteProduccion(List<ProduccionItemRequest> items) {
        procesarLoteProduccion(items, null);
    }
}