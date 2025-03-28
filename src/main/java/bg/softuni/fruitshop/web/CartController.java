package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.cartItem.model.CartItem;
import bg.softuni.fruitshop.cartItem.service.CartItemService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartItemService cartItemService;

    @Autowired
    public CartController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable UUID productId,
                            @AuthenticationPrincipal AuthenticationMetadata user,
                            @RequestParam(defaultValue = "1") @Min(1) int quantity) {

        cartItemService.addToCart(user.getUserId(), productId, quantity);
        return "redirect:/products";
    }

    @GetMapping
    public ModelAndView viewCart(@AuthenticationPrincipal AuthenticationMetadata user) {
        List<CartItem> items = cartItemService.getCartItemsByUser(user.getUserId());

        ModelAndView modelAndView = new ModelAndView("shopping-cart");
        modelAndView.addObject("cartItems", items);

        BigDecimal total = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        modelAndView.addObject("totalPrice", total);
        return modelAndView;
    }

    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable UUID cartItemId,
                                 @AuthenticationPrincipal AuthenticationMetadata user) {
        cartItemService.removeItem(cartItemId, user.getUserId());
        return "redirect:/cart";
    }
}
