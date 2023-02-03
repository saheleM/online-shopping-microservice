package com.insa.gov.orderservice.repository;

import com.insa.gov.orderservice.model.OrderLineItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineItemRepository extends JpaRepository<OrderLineItems, Long> {
    List<OrderLineItems> findAllByOrderId(Long id);
}
