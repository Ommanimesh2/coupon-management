package com.monk.coupon.seeder;

import com.monk.coupon.model.Product;
import com.monk.coupon.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ProductSeeder implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private ProductRepository productRepository;
    private static final int NUM_PRODUCTS = 20;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedProducts();
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = IntStream.range(1, NUM_PRODUCTS + 1)
                    .mapToObj(i -> Product.builder()
                            .price(100.0 * i)
                            .build())
                    .collect(Collectors.toList());

            productRepository.saveAll(products);
        }
    }
}
