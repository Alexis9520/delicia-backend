package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.model.Comprobante;
import com.delicia.deliciabackend.model.Order;
import com.delicia.deliciabackend.model.OrderItem;
import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.ComprobanteRepository;
import com.delicia.deliciabackend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.io.ByteArrayOutputStream;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

@Service
public class ComprobanteService {

    @Autowired
    private ComprobanteRepository comprobanteRepository;
    @Autowired
    private OrderRepository orderRepository;

    public Comprobante generarComprobanteMock(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) throw new RuntimeException("Order not found");
        Order order = orderOpt.get();

        Usuario usuario = order.getUsuario();

        Comprobante comprobante = new Comprobante();
        comprobante.setTipo("boleta");
        comprobante.setSerie("B001");
        comprobante.setNumero(String.valueOf((int) (Math.random() * 90000 + 10000)));
        comprobante.setClienteNombre(usuario != null ? usuario.getName() : "Cliente Prueba");
        comprobante.setClienteDocumento(usuario != null ? usuario.getDocumento() : "00000000");
        comprobante.setFecha(LocalDateTime.now());
        comprobante.setTotal(order.getTotal());
        comprobante.setPdfUrl(null); // El PDF será servido por endpoint aparte
        comprobante.setXml("<xml>...mock...</xml>");
        comprobante.setMensaje("Comprobante aceptado");
        comprobante.setOrder(order);

        return comprobanteRepository.save(comprobante);
    }

    public Comprobante getComprobanteByOrderId(Long orderId) {
        return comprobanteRepository.findByOrderId(orderId)
                .stream().findFirst().orElse(null);
    }

    // Buscar comprobante por su id
    public Comprobante findById(Long comprobanteId) {
        return comprobanteRepository.findById(comprobanteId).orElse(null);
    }

    // Generar PDF real con los datos del comprobante usando iText
    public byte[] generarPdf(Long comprobanteId) {
        Comprobante c = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new RuntimeException("Comprobante not found"));
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título centrado
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("COMPROBANTE ELECTRÓNICO SUNAT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" ")); // Espacio

            // Encabezado de factura
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(80);
            headerTable.setSpacingBefore(10f);
            headerTable.setSpacingAfter(10f);

            headerTable.addCell(getCell("Tipo:", PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell(c.getTipo().toUpperCase(), PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell("Serie:", PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell(c.getSerie(), PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell("Número:", PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell(c.getNumero(), PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell("Fecha emisión:", PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell(c.getFecha().toString().replace("T", " "), PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell("Cliente:", PdfPCell.ALIGN_LEFT));
            headerTable.addCell(getCell(c.getClienteNombre() + " (" + c.getClienteDocumento() + ")", PdfPCell.ALIGN_LEFT));
            document.add(headerTable);

            // Tabla de productos
            PdfPTable prodTable = new PdfPTable(4);
            prodTable.setWidthPercentage(90);
            prodTable.setSpacingBefore(10f);
            prodTable.setSpacingAfter(10f);
            prodTable.setWidths(new int[]{50, 10, 15, 15});

            prodTable.addCell(getHeaderCell("Producto"));
            prodTable.addCell(getHeaderCell("Cant."));
            prodTable.addCell(getHeaderCell("P. Unitario"));
            prodTable.addCell(getHeaderCell("Subtotal"));

            // Obtiene los productos desde la orden asociada
            if (c.getOrder() != null && c.getOrder().getItems() != null) {
                for (OrderItem item : c.getOrder().getItems()) {
                    prodTable.addCell(getCell(item.getProduct().getName(), PdfPCell.ALIGN_LEFT));
                    prodTable.addCell(getCell(String.valueOf(item.getQuantity()), PdfPCell.ALIGN_CENTER));
                    prodTable.addCell(getCell("S/ " + item.getProduct().getPrice(), PdfPCell.ALIGN_RIGHT));
                    prodTable.addCell(getCell("S/ " + (item.getQuantity() * item.getProduct().getPrice()), PdfPCell.ALIGN_RIGHT));
                }
            } else {
                prodTable.addCell(getCell("Producto demo", PdfPCell.ALIGN_LEFT));
                prodTable.addCell(getCell("1", PdfPCell.ALIGN_CENTER));
                prodTable.addCell(getCell("S/ " + c.getTotal(), PdfPCell.ALIGN_RIGHT));
                prodTable.addCell(getCell("S/ " + c.getTotal(), PdfPCell.ALIGN_RIGHT));
            }
            document.add(prodTable);

            // Totales
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalTable.addCell(getCell("Total:", PdfPCell.ALIGN_LEFT));
            totalTable.addCell(getCell("S/ " + c.getTotal(), PdfPCell.ALIGN_RIGHT));
            document.add(totalTable);

            document.add(new Paragraph(" ")); // Espacio

            // Estado
            Font estadoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(0, 128, 0));
            Paragraph estado = new Paragraph(c.getMensaje(), estadoFont);
            estado.setAlignment(Element.ALIGN_CENTER);
            document.add(estado);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF error", e);
        }
    }

    // Helpers para formato de celda en tablas
    private PdfPCell getCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(new BaseColor(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorderWidth(1f);
        return cell;
    }
}