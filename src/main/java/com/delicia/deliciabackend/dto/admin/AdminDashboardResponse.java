package com.delicia.deliciabackend.dto.admin;

import java.util.List;

public class AdminDashboardResponse {
    public Kpis kpis;
    public List<ProductStat> lowStock;
    public List<ProductStat> topSold;
    public List<ProductStat> topMermas;
    public List<WorkerSales> ventasPorTrabajador;
    public double ventasMostradorPorHora;
    public List<InventoryMovementDto> ultimosMovimientos;

    public static class Kpis {
        public double tasaMermaPct;
        public double ventasTotalesHoy;
        public double mixOnlinePct;
        public double mixMostradorPct;
        public double ticketPromedio;
        public int pedidosTotalesHoy;
    }

    public static class ProductStat {
        public Long productId;
        public String name;
        public int value; // quantity
    }

    public static class WorkerSales {
        public Long userId;
        public String name;
        public double totalVentas;
        public int numeroPedidos;
    }

    public static class InventoryMovementDto {
        public Long id;
        public Long productoId;
        public int cantidad;
        public String tipo;
        public String motivo;
        public String referenciaTipo;
        public String referencia;
        public java.util.Date createdAt;
    }
}
