package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.repository.OrderRepository;
import bg.softuni.fruitshop.order.service.OrderItemService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;

    @Autowired
    public OrderController(OrderRepository orderRepository, OrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
    }

    @GetMapping("/confirmation")
    public String showConfirmation(@AuthenticationPrincipal AuthenticationMetadata user, Model model) {
        Order lastOrder = orderRepository.findTopByCustomer_IdOrderByDateCreatedDesc(user.getUserId());

        BigDecimal total = lastOrder.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("order", lastOrder);
        model.addAttribute("total", total);

        return "order-confirmation";
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllOrdersGrouped(Model model) {
        Map<Order, List<OrderItem>> groupedOrders = orderItemService.getGroupedCompletedOrders();
        model.addAttribute("groupedOrders", groupedOrders);
        return "admin-orders";
    }
}
