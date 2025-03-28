package bg.softuni.fruitshop.cartItem.service;

import bg.softuni.fruitshop.cartItem.model.CartItem;
import bg.softuni.fruitshop.cartItem.repository.CartItemRepository;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addToCart(UUID userId, UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CartItem cartItem = CartItem.builder()
                .product(product)
                .user(user)
                .quantity(quantity)
                .build();

        cartItemRepository.save(cartItem);
    }

    public List<CartItem> getCartItemsByUser(UUID userId) {
        return cartItemRepository.findAllByUserId(userId);
    }

    @Transactional
    public void removeItem(UUID cartItemId, UUID userId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new SecurityException("Not allowed to delete this item");
        }

        cartItemRepository.delete(item);
    }
}
