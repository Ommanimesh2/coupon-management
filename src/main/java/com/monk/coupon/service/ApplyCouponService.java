package com.monk.coupon.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monk.coupon.dto.request.ApplicableCouponDto;
import com.monk.coupon.dto.request.CartDto;
import com.monk.coupon.dto.request.CartItemDto;
import com.monk.coupon.dto.response.ApplicableCouponsResponse;
import com.monk.coupon.dto.response.UpdatedCartResponse;
import com.monk.coupon.model.BxGyProductQuantity;
import com.monk.coupon.model.coupon.BxGyDetails;
import com.monk.coupon.model.coupon.CartWiseDetails;
import com.monk.coupon.model.coupon.Coupon;
import com.monk.coupon.model.coupon.ProductWiseDetails;
import com.monk.coupon.repository.CouponRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApplyCouponService {
    @Autowired
    private CouponRepository couponRepository;

    public ApplicableCouponsResponse getApplicableCoupons(CartDto cart) {
        List<Coupon> coupons = couponRepository.findAll();
        List<ApplicableCouponDto> applicableCoupons = new ArrayList<>();

        for (Coupon coupon : coupons) {
            double discount = 0;

            switch (coupon.getType()) {
                case CART_WISE:
                    discount = applyCartWiseCoupon(coupon, cart);
                    break;

                case PRODUCT_WISE:
                    discount = cart.getItems().stream()
                            .mapToDouble(item -> applyProductWiseCoupon(coupon, item))
                            .sum();
                    break;

                case BXGY:

                    List<CartItemDto> updatedItems = applyBxGyCoupon(coupon, cart);
                    discount = updatedItems.stream()
                            .mapToDouble(item -> item.getTotalDiscount())
                            .sum();
                    break;
            }

            if (discount > 0) {
                applicableCoupons.add(new ApplicableCouponDto(coupon.getId(), coupon.getType(), discount));
            }
        }

        return new ApplicableCouponsResponse(applicableCoupons);
    }

    public UpdatedCartResponse applyCoupon(Long couponId, CartDto cart) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + couponId));

        log.info("Applying coupon: {}", coupon);
        log.info("Cart: {}", cart);

        double totalDiscount = 0;
        List<CartItemDto> updatedItems = new ArrayList<>();
        double totalPrice;
        double finalPrice;

        switch (coupon.getType()) {
            case CART_WISE:
                totalDiscount = applyCartWiseCoupon(coupon, cart);
                totalPrice = cart.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();
                finalPrice = totalPrice - totalDiscount;
                log.info("Cart CartWise total: {}, discount: {}, finalPrice: {}", totalPrice, totalDiscount,
                        finalPrice);
                return new UpdatedCartResponse(cart.getItems(), totalPrice, totalDiscount, finalPrice);

            case PRODUCT_WISE:
                for (CartItemDto item : cart.getItems()) {
                    double discount = applyProductWiseCoupon(coupon, item);
                    log.info("Applying discount on product {} with quantity {}: {}", item.getProductId(),
                            item.getQuantity(), discount);
                    updatedItems
                            .add(new CartItemDto(item.getProductId(), item.getQuantity(), item.getPrice(), discount));
                    totalDiscount += discount;
                }
                totalPrice = updatedItems.stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();
                finalPrice = totalPrice - totalDiscount;
                log.info("Cart ProductWise total: {}, discount: {}, finalPrice: {}", totalPrice, totalDiscount,
                        finalPrice);
                return new UpdatedCartResponse(updatedItems, totalPrice, totalDiscount, finalPrice);

            case BXGY:
                updatedItems = applyBxGyCoupon(coupon, cart);
                totalDiscount = updatedItems.stream()
                        .mapToDouble(item -> item.getTotalDiscount())
                        .sum();
                totalPrice = updatedItems.stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();
                finalPrice = totalPrice - totalDiscount;
                log.info("Cart BxGyWise total: {}, discount: {}, finalPrice: {}", totalPrice, totalDiscount,
                        finalPrice);
                return new UpdatedCartResponse(updatedItems, totalPrice, totalDiscount, finalPrice);

            default:
                throw new IllegalArgumentException("Invalid coupon type");
        }
    }

    private double applyCartWiseCoupon(Coupon coupon, CartDto cart) {
        double cartTotal = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        log.info("Cart total: {}", cartTotal);

        CartWiseDetails details = (CartWiseDetails) coupon.getDetails();

        log.info("Threshold: {}", details.getThreshold());

        if (cartTotal > details.getThreshold()) {
            log.info("Applying cart-wise coupon");
            return cartTotal * (details.getDiscount() / 100);
        }

        log.info("Cart-wise coupon not applicable");
        return 0;
    }

    private double applyProductWiseCoupon(Coupon coupon, CartItemDto item) {
        log.info("Applying product-wise coupon: {}", coupon);
        log.info("Item: {}", item);

        ProductWiseDetails details = (ProductWiseDetails) coupon.getDetails();
        if (item.getProductId().equals(details.getProductId())) {
            log.info("Applying discount on product {} with quantity {}", item.getProductId(), item.getQuantity());
            return item.getPrice() * item.getQuantity() * (details.getDiscount() / 100);
        }

        log.info("Product-wise coupon not applicable on product {}", item.getProductId());
        return 0;
    }

    private List<CartItemDto> applyBxGyCoupon(Coupon coupon, CartDto cart) {
        log.info("Applying BxGy coupon: {}", coupon);
        log.info("Cart: {}", cart);

        BxGyDetails details = (BxGyDetails) coupon.getDetails();
        List<CartItemDto> updatedItems = new ArrayList<>();

        Map<Long, Integer> buyProductQuantities = new HashMap<>();
        Map<Long, Double> itemDiscountMap = new HashMap<>();
        log.info("cart.getItems(): {}", cart.getItems());
        log.info("details.getBuyProducts(): {}", details.getBuyProducts());
        for (CartItemDto item : cart.getItems()) {
            details.getBuyProducts().forEach(buyProduct -> {
                if (buyProduct.getProduct().getProduct_id().equals(item.getProductId())) {
                    buyProductQuantities.put(item.getProductId(), item.getQuantity());
                }
            });
        }
        log.info("buyProductQuantities: {}", buyProductQuantities);
        int totalTimesCouponCanBeApplied = 0;

        // Calculate how many times the coupon can be applied based on the quantities of
        // buy products
        for (BxGyProductQuantity buyProduct : details.getBuyProducts()) {
            int requiredQuantity = buyProduct.getQuantity();
            int cartQuantity = buyProductQuantities.getOrDefault(buyProduct.getProduct().getProduct_id(), 0);

            if (cartQuantity < requiredQuantity) {
                log.info("Not enough quantity of product {} to meet the buy condition.",
                        buyProduct.getProduct().getProduct_id());
                return updatedItems;
            }

            int timesForThisProduct = cartQuantity / requiredQuantity;
            totalTimesCouponCanBeApplied += timesForThisProduct;
        }

        // Apply the repetition limit
        totalTimesCouponCanBeApplied = Math.min(totalTimesCouponCanBeApplied, details.getRepetitionLimit());
        log.info("Total times BxGy coupon can be applied: {}", totalTimesCouponCanBeApplied);

        Map<Long, Integer> getProductQuantitiesMap = new HashMap<>();

        for (CartItemDto item : cart.getItems()) {
            details.getGetProducts().forEach(getProduct -> {
                if (getProduct.getProduct().getProduct_id().equals(item.getProductId())) {
                    getProductQuantitiesMap.put(item.getProductId(), item.getQuantity());
                }
            });
        }
        log.info("getProductQuantitiesMap: {}", getProductQuantitiesMap);

        // Apply coupon logic while decrementing the totalTimesCouponCanBeApplied
        while (totalTimesCouponCanBeApplied-- > 0) {
            for (BxGyProductQuantity getProduct : details.getGetProducts()) {
                Long productId = getProduct.getProduct().getProduct_id();
                int requiredQuantity = getProduct.getQuantity();
                int availableQuantity = getProductQuantitiesMap.getOrDefault(productId, 0);

                if (availableQuantity >= requiredQuantity) {
                    double productPrice = getProduct.getProduct().getPrice();
                    double discount = requiredQuantity * productPrice;
                    log.info("Applying discount for {} units of product {} at price {}",
                            requiredQuantity, productId, productPrice);

                    // Add discount for this iteration
                    itemDiscountMap.put(productId, itemDiscountMap.getOrDefault(productId, 0.0) + discount);

                    // Decrease the quantity in the map
                    getProductQuantitiesMap.put(productId, availableQuantity - requiredQuantity);
                }
            }
        }

        for (CartItemDto item : cart.getItems()) {
            if (!buyProductQuantities.containsKey(item.getProductId())) {
                double discount = itemDiscountMap.getOrDefault(item.getProductId(), 0.0);
                updatedItems.add(new CartItemDto(item.getProductId(), item.getQuantity(), item.getPrice(), discount));
            } else {
                updatedItems.add(item);
            }
        }

        log.info("Updated cart items with discounts (excluding buy products): {}", updatedItems);
        return updatedItems;
    }
}
