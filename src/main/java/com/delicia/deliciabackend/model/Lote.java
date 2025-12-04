package com.delicia.deliciabackend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código legible: L-YYYYMMDDHHMMSS-<id> o el que envíe el frontend
    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private String createdBy; // opcional: email/usuario que registró

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}