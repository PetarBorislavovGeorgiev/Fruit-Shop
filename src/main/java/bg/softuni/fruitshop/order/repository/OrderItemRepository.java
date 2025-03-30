package bg.softuni.fruitshop.order.repository;

import bg.softuni.fruitshop.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findAllByOrderCustomerId(UUID userId);

    @Query("SELECT item FROM OrderItem item WHERE item.order IS NOT NULL ORDER BY item.order.createdOn DESC")
    List<OrderItem> findAllWithOrderSortedByOrderCreationDateDesc();

    List<OrderItem> findAllByOrderIsNotNullOrderByOrder_CreatedOnDesc();
}
