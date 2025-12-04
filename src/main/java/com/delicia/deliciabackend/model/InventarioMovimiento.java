package com.delicia.deliciabackend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productoId;

    // cantidad positiva para ENTRADA, negativa para SALIDA
    private int cantidad;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    @Enumerated(EnumType.STRING)
    private MotivoMovimiento motivo;

    // referencia flexible legacy (seguir manteniendo por compatibilidad)
    private String referenciaTipo;
    private String referencia;

    // Nuevo: relacion al lote (si aplica)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    private Date createdAt;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public MotivoMovimiento getMotivo() { return motivo; }
    public void setMotivo(MotivoMovimiento motivo) { this.motivo = motivo; }

    public String getReferenciaTipo() { return referenciaTipo; }
    public void setReferenciaTipo(String referenciaTipo) { this.referenciaTipo = referenciaTipo; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}