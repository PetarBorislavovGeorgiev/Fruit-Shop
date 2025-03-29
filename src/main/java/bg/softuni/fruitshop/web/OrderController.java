package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.repository.OrderRepository;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.math.BigDecimal;

@Controller
public class OrderController {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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

}
