package com.delicia.deliciabackend.dto;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> items;
    private AddressRequest address;
    private String paymentMethod;
    private double total;
    private String paymentIntentId;

    // NUEVO: Campos para venta mostrador
    private String canal;
    private String nombreCliente;
    private String documentoCliente;

    // Getters and setters
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public AddressRequest getAddress() { return address; }
    public void setAddress(AddressRequest address) { this.address = address; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }
}