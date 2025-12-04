package com.delicia.deliciabackend.dto;

/**
 * DTO para cada item en el lote de producción.
 * Se añade loteId opcional para enlazar movimientos con un identificador de lote.
 */
public class ProduccionItemRequest {
    private Long productoId;
    private int cantidad;
    private String loteId; // opcional: código o identificador del lote

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getLoteId() { return loteId; }
    public void setLoteId(String loteId) { this.loteId = loteId; }
}