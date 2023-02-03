package com.insa.gov.orderservice.service;

import com.insa.gov.orderservice.dto.InventoryResponse;
import com.insa.gov.orderservice.dto.OrderLineItemsDto;
import com.insa.gov.orderservice.dto.OrderRequest;
import com.insa.gov.orderservice.dto.OrderResponseUpdate;
import com.insa.gov.orderservice.model.Order;
import com.insa.gov.orderservice.model.OrderLineItems;
import com.insa.gov.orderservice.repository.OrderLineItemRepository;
import com.insa.gov.orderservice.repository.OrderRepository;



import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


import javax.persistence.EntityNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderLineItemRepository orderLineItemRepository;
    private final WebClient.Builder webClientBuilder;


    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(getOrderNoWithYear());
        order.setOrderTable(orderRequest.getOrderTable());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDtoList)
                .toList();


        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);
        if (allProductsInStock) {
            orderRepository.save(order);
            return "Place Order is Successful";
        } else {
            throw new IllegalArgumentException("Product is not stock, Please order again!");
        }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());


        return orderLineItems;
    }

    private OrderLineItems mapToDtoList(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setId(orderLineItemsDto.getId());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());



        return orderLineItems;
    }

    private List<OrderLineItems> mapDtoListToEntityList(Order o,List<OrderLineItemsDto> orderLineItemsListDto) {
        List<OrderLineItems> orderLineItems = new ArrayList<>();
        OrderLineItems orders;
        for (int i = 0; i < orderLineItemsListDto.size(); i++) {
            orders =new OrderLineItems();
            orders.setId(orderLineItemsListDto.get(i).getId());
            orders.setSkuCode(orderLineItemsListDto.get(i).getSkuCode());
            orders.setPrice(orderLineItemsListDto.get(i).getPrice());
            orders.setQuantity(orderLineItemsListDto.get(i).getQuantity());
            orders.setOrder(o);
            orderLineItems.add(orders);
        }






        return orderLineItems;
    }


    public String getOrderNoWithYear() {

        String refNo; // Format Of Reference Number REF/001/07
        Integer seqNo = 0;

        DateFormat dateFormat = new SimpleDateFormat("yyyy");

        Date _date = new Date();
        String yourDate = dateFormat.format(_date);

        Order orderInfo = new Order();
        orderInfo = orderRepository.findOrderMax();
        String sequNo = null;

        if (orderInfo != null) {

            Integer seqRefNo = Integer.parseInt(orderInfo.getOrderNumber().split("/")[1]);
            String year = orderInfo.getOrderNumber().split("/")[2];
            if (year.equals(yourDate)) {
                seqNo = seqRefNo + 1;

                if (String.valueOf(seqNo).length() == 1) {
                    sequNo = "000000" + seqNo;
                } else if (String.valueOf(seqNo).length() == 2) {
                    sequNo = "00000" + seqNo;
                } else if (String.valueOf(seqNo).length() == 3) {
                    sequNo = "0000" + seqNo;
                } else if (String.valueOf(seqNo).length() == 4) {
                    sequNo = "000" + seqNo;
                } else if (String.valueOf(seqNo).length() == 5) {
                    sequNo = "00" + seqNo;
                } else if (String.valueOf(seqNo).length() == 6) {
                    sequNo = "0" + seqNo;
                }

                refNo = "OR_NO/" + sequNo + "/" + dateFormat.format(_date); // remain work get current year
            } else {
                seqNo = seqNo + 1;

                if (String.valueOf(seqNo).length() == 1) {
                    sequNo = "000000" + seqNo;
                }
                refNo = "OR_NO/" + sequNo + "/" + dateFormat.format(_date);
            }
        } else {
            seqNo = seqNo + 1;

            if (String.valueOf(seqNo).length() == 1) {
                sequNo = "000000" + seqNo;
            }
            refNo = "OR_NO/" + sequNo + "/" + dateFormat.format(_date);
        }
        return refNo;
    }


    public void updateParentAndChildrenList(Long id, List<OrderLineItemsDto> orderLineItemsDtoList) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Parent Not Found"));

        List<OrderLineItems> orderLineItemsList = orderLineItemRepository.findAllByOrderId(id);

        System.out.printf("====order list size ========="+order.getOrderLineItemsList().size());
        System.out.printf("====orderLineItems list size ========="+order.getOrderLineItemsList().size());
        orderLineItemsList.forEach(orderLineItems -> {
            orderLineItemsDtoList.forEach(c->{
                if(c.getId() == orderLineItems.getOrder().getId()){
                    orderLineItems.setQuantity(c.getQuantity());
                    orderLineItems.setPrice(c.getPrice());
                    orderLineItems.setSkuCode(c.getSkuCode());

                }
            });
        });
        orderRepository.saveAndFlush(order);
        orderLineItemRepository.saveAll(orderLineItemsList);
    }


    public OrderLineItems updateOrderLineItems(Long id, OrderLineItemsDto orderLineItemsDto) {

        return orderLineItemRepository.findById(id)
                .map(orderLineItems -> {
                    orderLineItems.setId(orderLineItemsDto.getId());
                    orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
                    orderLineItems.setPrice(orderLineItemsDto.getPrice());
                    orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
                    return orderLineItemRepository.save(orderLineItems);
                }).orElseThrow(() -> new RuntimeException("Order line Item Not Found"));


    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseUpdate> getOrderList(List<String> orderNumber) {
        return orderRepository.findByOrderNumberIn(orderNumber).stream()
                .map(order ->
                        OrderResponseUpdate.builder()
                                .orderNumber(order.getOrderNumber())
                                .orderTable(order.getOrderTable())
                                .orderLineItemsList(order.getOrderLineItemsList())

                                .build()
                ).toList();
    }


    public void updateOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(getOrderNoWithYear());
        order.setOrderTable(orderRequest.getOrderTable());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();


        order.setOrderLineItemsList(orderLineItems);
    }

    public Order updateParentAndChildren(Long id, OrderRequest orderRequest) {
        System.out.println("======order request in service===" +orderRequest.getOrderLineItemsDtoList().size());
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
        List<OrderLineItems> convertedList=mapDtoListToEntityList(order,orderRequest.getOrderLineItemsDtoList());

        order.getOrderLineItemsList().replaceAll(l1 -> convertedList.stream()
                .filter(l2 -> l2.getOrder().getId().equals(l1.getOrder().getId()))
                .findAny()
                .orElse(l1));
        for (OrderLineItems orderLine1:order.getOrderLineItemsList()) {
            System.out.println("===== parent id and name ====="+orderLine1.getOrder().getId()+ "  "+orderLine1.getSkuCode());

        }



//        for (OrderLineItems orderItems: order.getOrderLineItemsList()) {
//
//            for(OrderLineItemsDto orderLineItemsDto: orderRequest.getOrderLineItemsDtoList()) {
//                    if(orderLineItemsDto.getOrder_id()==orderItems.getId()){
//                        order.setOrderTable(orderRequest.getOrderTable());
//                        orderItems.setOrder(order);
//                        orderItems.setPrice(orderLineItemsDto.getPrice());
//                        orderItems.setQuantity(orderLineItemsDto.getQuantity());
//                        orderItems.setSkuCode(orderLineItemsDto.getSkuCode());
//
//
//                    }
//
//                order.setOrderLineItemsList(orderItems.getOrder().getOrderLineItemsList());
//
//            }

//        }


        return orderRepository.saveAndFlush(order);


    }

}
