package bg.softuni.fruitshop.product;


import bg.softuni.fruitshop.product.model.CategoryType;
import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.product.service.ProductService;
import bg.softuni.fruitshop.web.dto.CreateProductRequest;
import bg.softuni.fruitshop.web.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class ProductServiceUTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;


    @Test
    void createProduct_shouldSaveCorrectProduct() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Banana")
                .price(BigDecimal.valueOf(1.99))
                .category(CategoryType.FRUIT)
                .build();

        productService.createProduct(request);

        verify(productRepository, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getName()).isEqualTo("Banana");
        assertThat(savedProduct.getPrice()).isEqualByComparingTo("1.99");
        assertThat(savedProduct.getCategory()).isEqualTo(CategoryType.FRUIT);
        assertThat(savedProduct.getCreatedOn()).isNotNull();
        assertThat(savedProduct.getUpdatedOn()).isNotNull();
    }

    @Test
    void listAllProducts_shouldReturnMappedResponses() {

        Product product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Apple")
                .price(BigDecimal.valueOf(1.00))
                .category(CategoryType.FRUIT)
                .build();

        Product product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Carrot")
                .price(BigDecimal.valueOf(0.80))
                .category(CategoryType.VEGETABLE)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponse> result = productService.listAllProducts();

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getName()).isEqualTo("Apple");
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("1.00");
        assertThat(result.get(0).getCategory()).isEqualTo(CategoryType.FRUIT);

        assertThat(result.get(1).getName()).isEqualTo("Carrot");
        assertThat(result.get(1).getPrice()).isEqualByComparingTo("0.80");
        assertThat(result.get(1).getCategory()).isEqualTo(CategoryType.VEGETABLE);
    }

    @Test
    void deleteProduct_shouldCallRepositoryWithCorrectId() {

        UUID productId = UUID.randomUUID();

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void getProductById_shouldReturnMappedProductResponse() {

        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Apple")
                .price(BigDecimal.valueOf(2.50))
                .category(CategoryType.FRUIT)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        var result = productService.getProductById(productId);

        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Apple");
        assertThat(result.getPrice()).isEqualByComparingTo("2.50");
        assertThat(result.getCategory()).isEqualTo(CategoryType.FRUIT);
    }

    @Test
    void getProductById_shouldThrowExceptionIfNotFound() {

        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                NoSuchElementException.class,
                () -> productService.getProductById(productId)
        );
    }



}
