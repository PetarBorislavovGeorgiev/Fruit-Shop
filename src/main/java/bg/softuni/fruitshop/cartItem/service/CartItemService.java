package bg.softuni.fruitshop.cartItem.service;

import bg.softuni.fruitshop.address.model.Address;
import bg.softuni.fruitshop.cartItem.model.CartItem;
import bg.softuni.fruitshop.cartItem.repository.CartItemRepository;
import bg.softuni.fruitshop.exception.MissingAddressException;
import bg.softuni.fruitshop.notification.client.NotificationClient;
import bg.softuni.fruitshop.notification.client.dto.NotificationRequest;
import bg.softuni.fruitshop.notification.client.dto.UpsertNotificationPreference;
import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.model.OrderItem;
import bg.softuni.fruitshop.order.repository.OrderRepository;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationClient notificationClient;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository, NotificationClient notificationClient) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.notificationClient = notificationClient;
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

    @Transactional
    public void placeOrder(UUID userId) {

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot place order: cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Address address = user.getAddresses().stream().findFirst()
                .orElseThrow(() -> new MissingAddressException("You must add an address before placing an order."));


        Order order = Order.builder()
                .customer(user)
                .address(address)
                .dateCreated(LocalDateTime.now())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProduct().getPrice())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setItems(orderItems);

        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        notificationClient.upsertNotificationPreference(
                UpsertNotificationPreference.builder()
                        .userId(user.getId())
                        .notificationEnabled(true)
                        .type("EMAIL")
                        .contactInfo(user.getEmail())
                        .build()
        );


        StringBuilder productDetails = new StringBuilder("Items in your order:\n");

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
            productDetails.append("- ")
                    .append(item.getProduct().getName())
                    .append(" x").append(item.getQuantity())
                    .append(" @ ").append(String.format(Locale.ENGLISH, "%.2f", item.getPrice()))
                    .append(" BGN\n");
        }
        productDetails.append("\n");
        productDetails.append("Total: ").append(String.format(Locale.ENGLISH, "%.2f", total)).append(" BGN");


        notificationClient.sendNotification(
                NotificationRequest.builder()
                        .userId(user.getId())
                        .subject("Thanks for your order!")
                        .body("Order #" + order.getId() + " placed successfully.\n\n" + productDetails)
                        .build()
        );

    }

    public int getCartItemCount(UUID userId) {
        return cartItemRepository.countByUserId(userId);
    }

}
