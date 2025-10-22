package com.delicia.deliciabackend.dto;

import java.util.List;
import java.util.Date;

public class OrderResponse {
    private Long id;
    private String paymentMethod;
    private double total;
    private String paymentIntentId;
    private AddressResponse address;
    private List<OrderItemResponse> items;
    private String status;
    private Date createdAt;

    // NUEVO: Campos para venta mostrador
    private String canal;
    private String nombreCliente;
    private String documentoCliente;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    public AddressResponse getAddress() { return address; }
    public void setAddress(AddressResponse address) { this.address = address; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }
}