package bg.softuni.fruitshop.order.repository;


import bg.softuni.fruitshop.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Order findTopByCustomer_IdOrderByDateCreatedDesc(UUID userId);

}
