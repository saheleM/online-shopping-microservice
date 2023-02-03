package com.insa.gov.orderservice.dto;

import com.insa.gov.orderservice.model.OrderLineItems;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemsDto  {
    private Long Id;
    private String skuCode;
    private BigInteger price;
    private Integer quantity;
    private Long order_id;
}
