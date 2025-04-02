package bg.softuni.fruitshop.cartItem;


import bg.softuni.fruitshop.TestBuilder;
import bg.softuni.fruitshop.cartItem.model.CartItem;
import bg.softuni.fruitshop.cartItem.repository.CartItemRepository;
import bg.softuni.fruitshop.cartItem.service.CartItemService;
import bg.softuni.fruitshop.exception.MissingAddressException;
import bg.softuni.fruitshop.notification.client.NotificationClient;
import bg.softuni.fruitshop.order.model.Order;
import bg.softuni.fruitshop.order.repository.OrderRepository;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.user.model.User;
import bg.softuni.fruitshop.user.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import bg.softuni.fruitshop.product.model.Product;
import org.junit.jupiter.api.Test;

import org.mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class CartItemServiceUTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CartItemService cartItemService;


    @Test
    void addToCart_shouldSaveCartItemCorrectly() {

        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .id(productId)
                .name("Apple")
                .price(BigDecimal.valueOf(2.5))
                .build();

        User user = User.builder()
                .id(userId)
                .username("TestUser")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);


        cartItemService.addToCart(userId, productId, 3);


        verify(cartItemRepository, times(1)).save(cartItemCaptor.capture());

        CartItem savedItem = cartItemCaptor.getValue();
        assertThat(savedItem.getProduct()).isEqualTo(product);
        assertThat(savedItem.getUser()).isEqualTo(user);
        assertThat(savedItem.getQuantity()).isEqualTo(3);
    }



    @Test
    void addToCart_shouldThrowException_whenProductNotFound() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItemService.addToCart(userId, productId, 1)
        );

        assertThat(exception.getMessage()).isEqualTo("Product not found");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_shouldThrowException_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .id(productId)
                .name("Apple")
                .price(BigDecimal.valueOf(2.5))
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItemService.addToCart(userId, productId, 1)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void removeItem_shouldDeleteCartItem_whenUserOwnsIt() {
        UUID userId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        CartItem item = CartItem.builder()
                .id(cartItemId)
                .user(user)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));

        cartItemService.removeItem(cartItemId, userId);

        verify(cartItemRepository, times(1)).delete(item);
    }

    @Test
    void removeItem_shouldThrowException_whenItemNotFound() {
        UUID userId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItemService.removeItem(cartItemId, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("Item not found");
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void removeItem_shouldThrowSecurityException_whenUserDoesNotOwnItem() {
        UUID userId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        User anotherUser = User.builder().id(UUID.randomUUID()).build();

        CartItem item = CartItem.builder()
                .id(cartItemId)
                .user(anotherUser)
                .build();

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));

        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> cartItemService.removeItem(cartItemId, userId)
        );

        assertThat(exception.getMessage()).isEqualTo("Not allowed to delete this item");
        verify(cartItemRepository, never()).delete(any());
    }



    @Test
    void getCartItemsByUser_shouldReturnUserCartItems() {
        UUID userId = UUID.randomUUID();

        CartItem item1 = CartItem.builder().id(UUID.randomUUID()).quantity(2).build();
        CartItem item2 = CartItem.builder().id(UUID.randomUUID()).quantity(5).build();

        when(cartItemRepository.findAllByUserId(userId)).thenReturn(List.of(item1, item2));

        var result = cartItemService.getCartItemsByUser(userId);

        assertThat(result).containsExactly(item1, item2);
        verify(cartItemRepository, times(1)).findAllByUserId(userId);
    }


    @Test
    void placeOrder_shouldCreateOrderSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .id(productId)
                .name("Banana")
                .price(BigDecimal.valueOf(1.50))
                .build();

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .username("TestUser")
                .addresses(Set.of(TestBuilder.aTestAddress()))
                .build();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .user(user)
                .quantity(2)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(userId)).thenReturn(List.of(cartItem));

        cartItemService.placeOrder(userId);

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartItemRepository, times(1)).deleteAll(List.of(cartItem));

        verify(notificationClient).upsertNotificationPreference(any());
        verify(notificationClient).sendNotification(any());
    }

    @Test
    void placeOrder_shouldThrowWhenCartIsEmpty() {
        UUID userId = UUID.randomUUID();

        when(cartItemRepository.findAllByUserId(userId)).thenReturn(List.of());

        assertThatThrownBy(() -> cartItemService.placeOrder(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cart is empty");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void placeOrder_shouldThrowWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        CartItem dummyItem = CartItem.builder()
                .id(UUID.randomUUID())
                .quantity(1)
                .build();

        when(cartItemRepository.findAllByUserId(userId)).thenReturn(List.of(dummyItem));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.placeOrder(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(notificationClient);
    }

    @Test
    void placeOrder_shouldThrowWhenUserHasNoAddress() {
        UUID userId = UUID.randomUUID();
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Apple")
                .price(BigDecimal.valueOf(2.5))
                .build();

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .addresses(Set.of()) // <- без адрес
                .build();

        CartItem cartItem = CartItem.builder()
                .product(product)
                .user(user)
                .quantity(1)
                .build();

        when(cartItemRepository.findAllByUserId(userId)).thenReturn(List.of(cartItem));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> cartItemService.placeOrder(userId))
                .isInstanceOf(MissingAddressException.class)
                .hasMessageContaining("add an address");

        verify(orderRepository, never()).save(any());
        verify(notificationClient, never()).sendNotification(any());
    }




}
