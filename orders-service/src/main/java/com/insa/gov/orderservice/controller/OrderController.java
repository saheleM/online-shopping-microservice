package com.insa.gov.orderservice.controller;

import com.insa.gov.orderservice.dto.OrderLineItemsDto;
import com.insa.gov.orderservice.dto.OrderRequest;
import com.insa.gov.orderservice.dto.OrderResponseUpdate;
import com.insa.gov.orderservice.model.Order;
import com.insa.gov.orderservice.model.OrderLineItems;
import com.insa.gov.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() -> orderService.placeOrder(orderRequest));
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, Please order after some time!");
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseUpdate> getOrderList(@RequestParam List<String> orderNumber) {
        return orderService.getOrderList(orderNumber);

    }

    @PutMapping("/updates/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String updateParentAndChildren(@PathVariable Long id, @RequestBody OrderRequest orderRequest) {
        System.out.printf("====list of order request ==" + orderRequest.getOrderLineItemsDtoList().size());
        orderService.updateParentAndChildren(id, orderRequest);
        return "update order list";
    }


//    @PostMapping("/new")
//    @ResponseStatus(HttpStatus.CREATED)
//    public Order createOrder(@RequestBody OrderRequest orderRequest) {
//        return orderService.createOrder(orderRequest.getOrder());
//
//    }


    @PutMapping("/updated/{id}")
    public void updateOrderInformationList(@PathVariable Long id,
                                           @RequestBody List<OrderLineItemsDto> orderLineItemsDtoList) {
        orderService.updateParentAndChildrenList(id, orderLineItemsDtoList);

    }

    @PutMapping("/updateList/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderLineItems updateOrder(@PathVariable Long id,
                                      @RequestBody OrderLineItemsDto orderRequestDto) {

        return orderService.updateOrderLineItems(id, orderRequestDto);

    }


}
