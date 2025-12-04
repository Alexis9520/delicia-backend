package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.admin.AdminDashboardResponse;
import com.delicia.deliciabackend.model.*;
import com.delicia.deliciabackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse out = new AdminDashboardResponse();
        AdminDashboardResponse.Kpis k = new AdminDashboardResponse.Kpis();

        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date startOfDay = c.getTime();

        List<Order> allOrders = orderRepository.findAll();

        // Ventas totales del dia y conteo de pedidos
        double ventasTotalesHoy = 0.0;
        int pedidosHoy = 0;
        int pedidosOnlineHoy = 0;
        int pedidosMostradorHoy = 0;

        for (Order o : allOrders) {
            if (o.getCreatedAt() != null && !o.getCreatedAt().before(startOfDay)) {
                ventasTotalesHoy += o.getTotal();
                pedidosHoy++;
                if ("mostrador".equalsIgnoreCase(o.getCanal())) pedidosMostradorHoy++;
                else pedidosOnlineHoy++;
            }
        }

        k.ventasTotalesHoy = ventasTotalesHoy;
        k.pedidosTotalesHoy = pedidosHoy;
        k.mixOnlinePct = pedidosHoy == 0 ? 0.0 : (100.0 * pedidosOnlineHoy / pedidosHoy);
        k.mixMostradorPct = pedidosHoy == 0 ? 0.0 : (100.0 * pedidosMostradorHoy / pedidosHoy);
        k.ticketPromedio = pedidosHoy == 0 ? 0.0 : (ventasTotalesHoy / pedidosHoy);

        // Merma: salida MERMA vs entrada PRODUCCION (usar todos los movimientos del dia)
        List<InventarioMovimiento> movimientos = inventarioMovimientoRepository.findAll();
        int mermaSalida = movimientos.stream()
                .filter(m -> m.getMotivo() == MotivoMovimiento.MERMA)
                .mapToInt(m -> Math.abs(m.getCantidad()))
                .sum();
        int produccionEntrada = movimientos.stream()
                .filter(m -> m.getMotivo() == MotivoMovimiento.PRODUCCION)
                .mapToInt(m -> Math.abs(m.getCantidad()))
                .sum();

        k.tasaMermaPct = (produccionEntrada == 0) ? 0.0 : (100.0 * mermaSalida / (double) produccionEntrada);

        out.kpis = k;

        // Low stock
        int threshold = 10;
        List<Product> products = productRepository.findAll();
        List<AdminDashboardResponse.ProductStat> low = products.stream()
                .filter(p -> p.getStock() < threshold)
                .map(p -> {
                    AdminDashboardResponse.ProductStat ps = new AdminDashboardResponse.ProductStat();
                    ps.productId = p.getId(); ps.name = p.getName(); ps.value = p.getStock(); return ps;
                }).collect(Collectors.toList());
        out.lowStock = low;

        // Top sold (motivo VENTA, tipo SALIDA) aggregate by product
        Map<Long, Integer> soldByProduct = movimientos.stream()
                .filter(m -> m.getMotivo() == MotivoMovimiento.VENTA)
                .collect(Collectors.groupingBy(InventarioMovimiento::getProductoId, Collectors.summingInt(m -> Math.abs(m.getCantidad()))));

        out.topSold = soldByProduct.entrySet().stream()
                .sorted(Map.Entry.<Long,Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> {
                    AdminDashboardResponse.ProductStat ps = new AdminDashboardResponse.ProductStat();
                    ps.productId = e.getKey();
                    Product p = products.stream().filter(x -> x.getId().equals(e.getKey())).findFirst().orElse(null);
                    ps.name = p != null ? p.getName() : "#" + e.getKey();
                    ps.value = e.getValue();
                    return ps;
                }).collect(Collectors.toList());

        // Top mermas
        Map<Long, Integer> mermaByProduct = movimientos.stream()
                .filter(m -> m.getMotivo() == MotivoMovimiento.MERMA)
                .collect(Collectors.groupingBy(InventarioMovimiento::getProductoId, Collectors.summingInt(m -> Math.abs(m.getCantidad()))));

        out.topMermas = mermaByProduct.entrySet().stream()
                .sorted(Map.Entry.<Long,Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> {
                    AdminDashboardResponse.ProductStat ps = new AdminDashboardResponse.ProductStat();
                    ps.productId = e.getKey();
                    Product p = products.stream().filter(x -> x.getId().equals(e.getKey())).findFirst().orElse(null);
                    ps.name = p != null ? p.getName() : "#" + e.getKey();
                    ps.value = e.getValue();
                    return ps;
                }).collect(Collectors.toList());

        // Ventas por trabajador (sumar orders mostrador asociadas a usuario)
        List<Order> mostradorOrders = allOrders.stream()
                .filter(o -> o.getCanal() != null && o.getCanal().equalsIgnoreCase("mostrador"))
                .collect(Collectors.toList());

        Map<Long, AdminDashboardResponse.WorkerSales> salesByUser = new HashMap<>();
        for (Order o : mostradorOrders) {
            Usuario u = o.getUsuario();
            if (u == null) continue; // si no está asociado, no contamos (se puede mejorar)
            salesByUser.computeIfAbsent(u.getId(), id -> {
                AdminDashboardResponse.WorkerSales ws = new AdminDashboardResponse.WorkerSales();
                ws.userId = id; ws.name = u.getName(); ws.totalVentas = 0; ws.numeroPedidos = 0; return ws;
            });
            AdminDashboardResponse.WorkerSales ws = salesByUser.get(u.getId());
            ws.totalVentas += o.getTotal();
            ws.numeroPedidos += 1;
        }
        out.ventasPorTrabajador = new ArrayList<>(salesByUser.values());

        // Ventas mostrador por hora (simple proxy): pedidosMostradorHoy / max(1, horasDesdeInicioDia)
        int hoursSinceStart = Math.max(1, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        out.ventasMostradorPorHora = (double) pedidosMostradorHoy / hoursSinceStart;

        // Ultimos movimientos (paginated, mostrar 20 últimos ordenados desc)
        List<InventarioMovimiento> last = inventarioMovimientoRepository.findAll(PageRequest.of(0, 20)).getContent();
        // convert to dto
        out.ultimosMovimientos = last.stream().sorted(Comparator.comparing(InventarioMovimiento::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(m -> {
                    AdminDashboardResponse.InventoryMovementDto dto = new AdminDashboardResponse.InventoryMovementDto();
                    dto.id = m.getId(); dto.productoId = m.getProductoId(); dto.cantidad = m.getCantidad(); dto.tipo = m.getTipo().name(); dto.motivo = m.getMotivo().name(); dto.referencia = m.getReferencia(); dto.referenciaTipo = m.getReferenciaTipo(); dto.createdAt = m.getCreatedAt(); return dto;
                }).collect(Collectors.toList());

        return out;
    }
}
