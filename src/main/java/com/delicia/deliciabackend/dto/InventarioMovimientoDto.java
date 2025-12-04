package com.delicia.deliciabackend.dto;

import java.util.Date;

/**
 * DTO para exponer movimientos de inventario de forma segura.
 * Incluye loteId en vez de la entidad Lote completa para evitar problemas de serialización.
 */
public class InventarioMovimientoDto {
    private Long id;
    private Long productoId;
    private int cantidad;
    private String tipo;
    private String motivo;
    private String referenciaTipo;
    private String referencia;
    private Long loteId; // referenciamos solo el id del lote
    private Date createdAt;

    public InventarioMovimientoDto() {}

    public InventarioMovimientoDto(Long id, Long productoId, int cantidad, String tipo, String motivo,
                                   String referenciaTipo, String referencia, Long loteId, Date createdAt) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.motivo = motivo;
        this.referenciaTipo = referenciaTipo;
        this.referencia = referencia;
        this.loteId = loteId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}