package com.insa.gov.productservice.service;

import com.insa.gov.productservice.dto.ProductRequest;
import com.insa.gov.productservice.dto.ProductResponse;
import com.insa.gov.productservice.model.Product;
import com.insa.gov.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;


    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .productDescription(productRequest.getProductDescription())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product {} is saved " + product.getId());

    }

    public List<ProductResponse> getAllProductList() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).collect(Collectors.toList());

    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder().id(product.getId()).productName(product.getProductName()).productDescription(product.getProductDescription()).price(product.getPrice()).build();
    }
}
