package bg.softuni.fruitshop.web;


import bg.softuni.fruitshop.cartItem.service.CartItemService;
import bg.softuni.fruitshop.security.AuthenticationMetadata;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final CartItemService cartItemService;

    public GlobalModelAttributes(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount(@AuthenticationPrincipal AuthenticationMetadata currentUser) {
        if (currentUser == null) {
            return 0;
        }

        return cartItemService.getCartItemCount(currentUser.getUserId());
    }
}
