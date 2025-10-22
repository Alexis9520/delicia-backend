package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.ComprobanteResponse;
import com.delicia.deliciabackend.model.Comprobante;
import com.delicia.deliciabackend.service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

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
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long comprobanteId) {
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