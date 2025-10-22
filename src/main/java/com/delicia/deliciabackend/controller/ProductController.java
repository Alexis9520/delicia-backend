package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.PaginatedResponse;
import com.delicia.deliciabackend.dto.ProductRequest;
import com.delicia.deliciabackend.model.Product;
import com.delicia.deliciabackend.service.ProductService;
import com.delicia.deliciabackend.service.ImageBBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageBBService imageBBService;

    // Obtener productos paginados y filtrados (para el cliente y trabajador)
    @GetMapping
    public PaginatedResponse<Product> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        return productService.getProducts(page, pageSize, search, category);
    }

    // Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear producto: multipart/form-data (archivo + texto)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductWithFile(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("category") String category,
            @RequestParam("stock") int stock,
            @RequestParam("available") boolean available,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "image", required = false) String imageUrl
    ) {
        String image;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                image = imageBBService.uploadImage(imageFile);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                image = imageUrl;
            } else {
                image = null;
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al subir la imagen: " + e.getMessage());
        }

        ProductRequest request = new ProductRequest(name, description, price, category, image, stock, available);
        Product product = productService.create(request);
        return ResponseEntity.ok(product);
    }

    // Crear producto: application/json (solo datos, link de imagen)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createProductWithJson(@RequestBody ProductRequest req) {
        Product product = productService.create(req);
        return ResponseEntity.ok(product);
    }

    // Actualizar producto: multipart/form-data
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductWithFile(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("category") String category,
            @RequestParam("stock") int stock,
            @RequestParam("available") boolean available,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "image", required = false) String imageUrl
    ) {
        String image;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                image = imageBBService.uploadImage(imageFile);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                image = imageUrl;
            } else {
                image = null;
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al subir la imagen: " + e.getMessage());
        }

        ProductRequest request = new ProductRequest(name, description, price, category, image, stock, available);
        Product product = productService.update(id, request);
        return ResponseEntity.ok(product);
    }

    // Actualizar producto: application/json
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProductWithJson(
            @PathVariable Long id,
            @RequestBody ProductRequest req
    ) {
        Product product = productService.update(id, req);
        return ResponseEntity.ok(product);
    }

    // Eliminar producto
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}