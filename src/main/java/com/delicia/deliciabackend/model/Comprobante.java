package com.delicia.deliciabackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // boleta, factura
    private String serie;
    private String numero;
    private String clienteNombre;
    private String clienteDocumento;
    private LocalDateTime fecha;
    private Double total;
    private String pdfUrl;

    @Column(columnDefinition = "TEXT")
    private String xml;
    private String mensaje;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Getters y Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getXml() { return xml; }
    public void setXml(String xml) { this.xml = xml; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}