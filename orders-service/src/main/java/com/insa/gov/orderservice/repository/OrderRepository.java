package com.insa.gov.orderservice.repository;

import com.insa.gov.orderservice.model.Order;
import io.netty.handler.codec.http2.Http2Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT * FROM `order-service`.t_order where id=(SELECT  max(id) FROM `order-service`.t_order)", nativeQuery = true)
    Order findOrderMax();


    List<Order> findByOrderNumberIn(List<String> orderNumber);
}
