package com.monk.coupon.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.monk.coupon.dto.request.AddCouponRequest;
import com.monk.coupon.dto.request.ProductDto;
import com.monk.coupon.enums.CouponType;
import com.monk.coupon.model.BxGyProductQuantity;
import com.monk.coupon.model.Product;
import com.monk.coupon.model.coupon.BxGyDetails;
import com.monk.coupon.model.coupon.CartWiseDetails;
import com.monk.coupon.model.coupon.Coupon;
import com.monk.coupon.model.coupon.ProductWiseDetails;
import com.monk.coupon.repository.BxDyDetailRepository;
import com.monk.coupon.repository.CouponRepository;
import com.monk.coupon.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BxDyDetailRepository bxDyDetailRepository;

    private Coupon addCartWiseCoupon(CartWiseDetails details) {
        Coupon coupon = Coupon.builder().details(details).type(CouponType.CART_WISE).build();
        return couponRepository.save(coupon);
    }

    private Coupon addProductWiseCoupon(ProductWiseDetails details) {
        Coupon coupon = Coupon.builder().details(details).type(CouponType.PRODUCT_WISE).build();
        return couponRepository.save(coupon);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    private Coupon addBxGyCoupon(BxGyDetails details) {
        Coupon coupon = Coupon.builder().details(details).type(CouponType.BXGY).build();
        return couponRepository.save(coupon);
    }

    public Coupon addCoupon(AddCouponRequest addCouponRequest) {
        CouponType type = addCouponRequest.getType();
        log.info("Adding coupon of type: {}", type);

        switch (type) {
            case CART_WISE:
                CartWiseDetails cartWiseDetails = new CartWiseDetails();
                cartWiseDetails.setThreshold(addCouponRequest.getDetails().getThreshold());
                cartWiseDetails.setDiscount(addCouponRequest.getDetails().getDiscount());
                log.info("Adding cart-wise coupon with threshold: {}, discount: {}", cartWiseDetails.getThreshold(),
                        cartWiseDetails.getDiscount());
                return addCartWiseCoupon(cartWiseDetails);

            case PRODUCT_WISE:
                ProductWiseDetails productWiseDetails = new ProductWiseDetails();
                Product product = productRepository.findById(addCouponRequest.getDetails().getProduct_id())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Product not found with id: " + addCouponRequest.getDetails().getProduct_id()));
                productWiseDetails.setProductId(product.getProduct_id());
                productWiseDetails.setDiscount(addCouponRequest.getDetails().getDiscount());
                log.info("Adding product-wise coupon for product: {}", productWiseDetails.getProductId());
                return addProductWiseCoupon(productWiseDetails);

            case BXGY:
                BxGyDetails bxGyDetails = new BxGyDetails();

                List<BxGyProductQuantity> buyProducts = addCouponRequest.getDetails().getBuyProducts().stream()
                        .map(this::mapToBxGyProductQuantity)
                        .collect(Collectors.toList());

                List<BxGyProductQuantity> getProducts = addCouponRequest.getDetails().getGetProducts().stream()
                        .map(this::mapToBxGyProductQuantity)
                        .collect(Collectors.toList());

                bxGyDetails.setBuyProducts(buyProducts);
                bxGyDetails.setGetProducts(getProducts);
                bxGyDetails.setRepetitionLimit(addCouponRequest.getDetails().getRepetitionLimit());
                log.info("Adding BxGy coupon with buy products: {}, get products: {}, repetition limit: {}",
                        bxGyDetails.getBuyProducts(),
                        bxGyDetails.getGetProducts(), bxGyDetails.getRepetitionLimit());

                return addBxGyCoupon(bxGyDetails);

            default:
                throw new IllegalArgumentException("Invalid coupon type");
        }
    }

    private BxGyProductQuantity mapToBxGyProductQuantity(ProductDto productDto) {
        Product product = productRepository.findById(productDto.getProduct_id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + productDto.getProduct_id()));

        return BxGyProductQuantity.builder()
                .product(product)
                .quantity(productDto.getQuantity())
                .build();
    }

    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));
    }

    public Coupon updateCoupon(Long id, AddCouponRequest couponRequest) {
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with id: " + id));

        CouponType type = couponRequest.getType();

        switch (type) {
            case CART_WISE:
                CartWiseDetails cartWiseDetails = (CartWiseDetails) existingCoupon.getDetails();
                cartWiseDetails.setThreshold(couponRequest.getDetails().getThreshold());
                cartWiseDetails.setDiscount(couponRequest.getDetails().getDiscount());
                break;

            case PRODUCT_WISE:
                ProductWiseDetails productWiseDetails = (ProductWiseDetails) existingCoupon.getDetails();
                Product product = productRepository.findById(couponRequest.getDetails().getProduct_id())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Product not found with id: " + couponRequest.getDetails().getProduct_id()));
                productWiseDetails.setProductId(product.getProduct_id());
                productWiseDetails.setDiscount(couponRequest.getDetails().getDiscount());
                break;

            case BXGY:
                BxGyDetails bxGyDetails = (BxGyDetails) existingCoupon.getDetails();

                List<BxGyProductQuantity> updatedBuyProducts = couponRequest.getDetails().getBuyProducts().stream()
                        .map(this::mapToBxGyProductQuantity)
                        .collect(Collectors.toList());

                List<BxGyProductQuantity> updatedGetProducts = couponRequest.getDetails().getGetProducts().stream()
                        .map(this::mapToBxGyProductQuantity)
                        .collect(Collectors.toList());

                bxGyDetails.setBuyProducts(updatedBuyProducts);
                bxGyDetails.setGetProducts(updatedGetProducts);
                bxGyDetails.setRepetitionLimit(couponRequest.getDetails().getRepetitionLimit());
                break;

            default:
                throw new IllegalArgumentException("Invalid coupon type");
        }

        return couponRepository.save(existingCoupon);
    }

    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new EntityNotFoundException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }

}
