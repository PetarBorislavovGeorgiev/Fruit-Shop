package bg.softuni.fruitshop.order.repository;

import bg.softuni.fruitshop.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findAllByOrderCustomerId(UUID userId);
}
