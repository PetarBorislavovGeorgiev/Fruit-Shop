package bg.softuni.fruitshop.product.service;

import bg.softuni.fruitshop.product.model.Product;
import bg.softuni.fruitshop.product.repository.ProductRepository;
import bg.softuni.fruitshop.web.dto.CreateProductRequest;
import bg.softuni.fruitshop.web.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .category(request.getCategory())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        productRepository.save(product);

    }

    public List<ProductResponse> listAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .category(product.getCategory())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteProduct(UUID productId) {
        productRepository.deleteById(productId);
    }
}
