package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.PaginatedResponse;
import com.delicia.deliciabackend.dto.ProductRequest;
import com.delicia.deliciabackend.model.Product;
import com.delicia.deliciabackend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Obtiene productos paginados y filtrados.
     * Este método espera que "page" sea 1-based (página 1 = primer bloque visible).
     * Se sanitizan page y pageSize para evitar índices negativos.
     *
     * @param page     página 1-based (si es <= 0 se normaliza a 1)
     * @param pageSize tamaño de página (si es <= 0 se normaliza a 12)
     * @param search   filtro de búsqueda por nombre (opcional)
     * @param category filtro de categoría (opcional)
     * @return PaginatedResponse<Product>
     */
    public PaginatedResponse<Product> getProducts(Integer page, Integer pageSize, String search, String category) {
        int p = (page == null) ? 1 : Math.max(1, page);           // garantía: p >= 1
        int ps = (pageSize == null || pageSize <= 0) ? 12 : pageSize; // garantía: ps >= 1

        Pageable pageable = PageRequest.of(p - 1, ps, Sort.by(Sort.Direction.ASC, "id")); // convertir a 0-based

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

        // Nota: Usa getContent() como argumento "data" del DTO para que el frontend lo vea como data.
        return new PaginatedResponse<>(
                productosPage.getContent(),                // <-- importante, debe ser llamado "data" en el DTO
                productosPage.getTotalElements(),
                p,            // page 1-based que devolvemos al cliente
                ps,
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

    @Transactional
    public synchronized void reducirStock(Long productoId, int cantidad) {
        Product producto = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad a reducir debe ser mayor que 0.");
        }

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getName());
        }

        producto.setStock(producto.getStock() - cantidad);
        productRepository.save(producto);
    }

    /**
     * Incrementa el stock del producto de forma sincronizada y transaccional.
     */
    @Transactional
    public synchronized void incrementarStock(Long productoId, int cantidad) {
        Product producto = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad a incrementar debe ser mayor que 0.");
        }

        producto.setStock(producto.getStock() + cantidad);
        productRepository.save(producto);
    }
}