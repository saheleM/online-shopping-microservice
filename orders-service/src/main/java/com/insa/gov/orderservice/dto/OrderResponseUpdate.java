package com.insa.gov.orderservice.dto;

import com.insa.gov.orderservice.model.Order;
import com.insa.gov.orderservice.model.OrderLineItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseUpdate {
    private String orderTable;
    private String orderNumber;
    private Order order;
    private List<OrderLineItems> orderLineItemsList;


}
