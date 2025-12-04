package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.ComprobanteResponse;
import com.delicia.deliciabackend.model.Comprobante;
import com.delicia.deliciabackend.service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.security.Principal;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    @Autowired
    private ComprobanteService comprobanteService;

    @PostMapping("/generar/{orderId}")
    public ComprobanteResponse generarComprobante(@PathVariable Long orderId) {
        Comprobante c = comprobanteService.generarComprobanteMock(orderId);
        return mapToResponse(c);
    }

    @GetMapping("/order/{orderId}")
    public ComprobanteResponse obtenerComprobantePorOrder(@PathVariable Long orderId) {
        Comprobante c = comprobanteService.getComprobanteByOrderId(orderId);
        return c != null ? mapToResponse(c) : null;
    }

    @GetMapping("/pdf/{comprobanteId}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long comprobanteId, Principal principal) {
        // Buscar comprobante
        Comprobante c = comprobanteService.findById(comprobanteId);
        // Si no existe, 404
        if (c == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar propietario (cliente) o roles TRABAJADOR/ADMIN
        String principalEmail = principal != null ? principal.getName() : null;
        boolean isOwner = false;
        if (principalEmail != null && c.getOrder() != null && c.getOrder().getUsuario() != null) {
            isOwner = principalEmail.equals(c.getOrder().getUsuario().getEmail());
        }

        boolean isWorkerOrAdmin = false;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            isWorkerOrAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .anyMatch(a -> "ROLE_TRABAJADOR".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        }

        if (!isOwner && !isWorkerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Generar y devolver PDF
        byte[] pdfBytes = comprobanteService.generarPdf(comprobanteId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("comprobante.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private ComprobanteResponse mapToResponse(Comprobante c) {
        ComprobanteResponse dto = new ComprobanteResponse();
        dto.setId(c.getId());
        dto.setTipo(c.getTipo());
        dto.setSerie(c.getSerie());
        dto.setNumero(c.getNumero());
        dto.setClienteNombre(c.getClienteNombre());
        dto.setClienteDocumento(c.getClienteDocumento());
        dto.setFecha(c.getFecha());
        dto.setTotal(c.getTotal());
        dto.setPdfUrl(null); // El PDF se descarga por endpoint
        dto.setXml(c.getXml());
        dto.setMensaje(c.getMensaje());
        dto.setOrderId(c.getOrder().getId());
        return dto;
    }
}