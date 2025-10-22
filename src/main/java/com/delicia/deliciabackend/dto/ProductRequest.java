package com.delicia.deliciabackend.dto;

public class ProductRequest {
    private String name;
    private String description;
    private double price;
    private String category;
    private String image;
    private int stock;
    private boolean available;

    // Constructor vacío
    public ProductRequest() {}

    // Constructor con todos los campos
    public ProductRequest(String name, String description, double price, String category, String image, int stock, boolean available) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.image = image;
        this.stock = stock;
        this.available = available;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}