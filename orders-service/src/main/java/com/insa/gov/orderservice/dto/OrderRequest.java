package com.insa.gov.orderservice.dto;

import com.insa.gov.orderservice.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String orderTable;
    private Order order;

    private List<OrderLineItemsDto> orderLineItemsDtoList=new ArrayList<>();
}
