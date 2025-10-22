package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.PaginatedResponse;
import com.delicia.deliciabackend.dto.ProductRequest;
import com.delicia.deliciabackend.model.Product;
import com.delicia.deliciabackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public PaginatedResponse<Product> getProducts(int page, int pageSize, String search, String category) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Product> productosPage;

        if (search != null && !search.isBlank() && category != null && !category.equals("all")) {
            productosPage = productRepository.findByNameContainingIgnoreCaseAndCategory(search, category, pageable);
        } else if (search != null && !search.isBlank()) {
            productosPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (category != null && !category.equals("all")) {
            productosPage = productRepository.findByCategory(category, pageable);
        } else {
            productosPage = productRepository.findAll(pageable);
        }

        return new PaginatedResponse<>(
                productosPage.getContent(),
                (int) productosPage.getTotalElements(),
                page,
                pageSize,
                productosPage.getTotalPages()
        );
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public Product create(ProductRequest req) {
        Product p = new Product();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setCategory(req.getCategory());
        p.setImage(req.getImage());
        p.setStock(req.getStock());
        p.setAvailable(req.isAvailable());
        return productRepository.save(p);
    }

    public Product update(Long id, ProductRequest req) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setCategory(req.getCategory());
        p.setImage(req.getImage());
        p.setStock(req.getStock());
        p.setAvailable(req.isAvailable());
        return productRepository.save(p);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}