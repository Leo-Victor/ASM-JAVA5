package com.poly.ASM.controller.admin;

import com.poly.ASM.dao.CategoryDAO;
import com.poly.ASM.dao.OrderDAO;
import com.poly.ASM.dao.OrderDetailDAO;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Category;
import com.poly.ASM.model.Order;
import com.poly.ASM.model.OrderDetail;
import com.poly.ASM.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/report")
public class ReportAController {

    @Autowired
    ProductDAO productDAO;

    @Autowired
    OrderDetailDAO orderDetailDAO;

    @Autowired
    OrderDAO orderDAO;

    // =============================================================
    // 1. THỐNG KÊ TỒN KHO (Inventory)
    // Trả về file: admin/reports.html
    // =============================================================
    @GetMapping("/index")
    public String inventory(Model model) {
        List<Product> items = productDAO.findAll();

        // Nhóm sản phẩm theo Loại hàng
        Map<String, List<Product>> grouped = items.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().getName()));

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (String key : grouped.keySet()) {
            List<Product> pList = grouped.get(key);
            Map<String, Object> row = new HashMap<>();

            row.put("group", key); // Tên loại hàng
            row.put("count", pList.size()); // Số lượng
            row.put("sum", pList.stream().mapToDouble(Product::getPrice).sum()); // Tổng tiền hàng
            row.put("avg", pList.size() > 0 ? pList.stream().mapToDouble(Product::getPrice).average().getAsDouble() : 0);

            reportData.add(row);
        }

        model.addAttribute("items", reportData);
        model.addAttribute("reportType", "inventory"); // Cờ để HTML biết đang ở tab nào
        model.addAttribute("title", "THỐNG KÊ HÀNG TỒN KHO");

        return "admin/reports";
    }

    // =============================================================
    // 2. THỐNG KÊ DOANH THU (Revenue)
    // Trả về file: admin/reports.html (Dùng chung với Inventory)
    // =============================================================
    @GetMapping("/revenue")
    public String revenue(Model model) {
        List<OrderDetail> details = orderDetailDAO.findAll();

        // Chỉ tính đơn thành công (Status = 2)
        List<OrderDetail> successDetails = details.stream()
                .filter(d -> d.getOrder().getStatus() == 2)
                .collect(Collectors.toList());

        // Nhóm theo Loại hàng -> Tính tổng doanh thu
        Map<String, DoubleSummaryStatistics> stats = successDetails.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getProduct().getCategory().getName(),
                        Collectors.summarizingDouble(d -> d.getPrice() * d.getQuantity())
                ));

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (String key : stats.keySet()) {
            DoubleSummaryStatistics s = stats.get(key);
            Map<String, Object> row = new HashMap<>();

            row.put("group", key);
            row.put("count", s.getCount()); // Số sản phẩm đã bán
            row.put("sum", s.getSum());     // Tổng doanh thu
            row.put("min", s.getMin());
            row.put("max", s.getMax());
            row.put("avg", s.getAverage());

            reportData.add(row);
        }

        model.addAttribute("items", reportData);
        model.addAttribute("reportType", "revenue");
        model.addAttribute("title", "BÁO CÁO DOANH THU THỰC TẾ");

        return "admin/reports";
    }

    // =============================================================
    // 3. KHÁCH HÀNG VIP (VIP Customers)
    // Trả về file: admin/report-vip.html (File riêng)
    // =============================================================
    @GetMapping("/vip")
    public String vip(Model model) {
        List<Order> orders = orderDAO.findAll();

        // Chỉ tính đơn thành công
        List<Order> successOrders = orders.stream()
                .filter(o -> o.getStatus() == 2)
                .collect(Collectors.toList());

        // Nhóm theo Khách hàng
        Map<String, DoubleSummaryStatistics> stats = successOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getAccount().getFullname(),
                        Collectors.summarizingDouble(Order::getTotalAmount)
                ));

        List<Map<String, Object>> reportData = new ArrayList<>();

        for (String key : stats.keySet()) {
            DoubleSummaryStatistics s = stats.get(key);
            Map<String, Object> row = new HashMap<>();

            row.put("group", key);          // Tên khách
            row.put("count", s.getCount()); // Số đơn mua
            row.put("sum", s.getSum());     // Tổng tiền đã chi

            reportData.add(row);
        }

        // Sắp xếp giảm dần theo Tổng tiền (Ai mua nhiều nhất lên đầu)
        reportData.sort((r1, r2) -> Double.compare((Double)r2.get("sum"), (Double)r1.get("sum")));

        model.addAttribute("items", reportData);

        // QUAN TRỌNG: Trả về đúng file report-vip.html
        return "admin/report-vip";
    }
}