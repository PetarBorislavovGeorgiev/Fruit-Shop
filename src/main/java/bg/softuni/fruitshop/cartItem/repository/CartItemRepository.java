package bg.softuni.fruitshop.cartItem.repository;


import bg.softuni.fruitshop.cartItem.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findAllByUserId(UUID userId);

    int countByUserId(UUID userId);

}
