package com.monk.coupon.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monk.coupon.dto.request.AddCouponRequest;
import com.monk.coupon.dto.request.CartDto;
import com.monk.coupon.dto.response.ApplicableCouponsResponse;
import com.monk.coupon.dto.response.UpdatedCartResponse;
import com.monk.coupon.model.coupon.Coupon;
import com.monk.coupon.service.ApplyCouponService;
import com.monk.coupon.service.CouponService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;
    @Autowired
    private ApplyCouponService applyCouponService;

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody AddCouponRequest couponRequest) {
        Coupon coupon = couponService.addCoupon(couponRequest);
        return new ResponseEntity<>(coupon, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return new ResponseEntity<>(coupons, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return new ResponseEntity<>(coupon, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id,
            @Valid @RequestBody AddCouponRequest couponRequest) {
        Coupon updatedCoupon = couponService.updateCoupon(id, couponRequest);
        return new ResponseEntity<>(updatedCoupon, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/applicable-coupons")
    public ResponseEntity<ApplicableCouponsResponse> getApplicableCoupons(@Valid @RequestBody CartDto cart) {
        log.info(cart.toString());
        System.out.println(cart.getItems());
        System.out.println(cart);
        ApplicableCouponsResponse response = applyCouponService.getApplicableCoupons(cart);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<UpdatedCartResponse> applyCoupon(@PathVariable Long id, @Valid @RequestBody CartDto cart) {
        UpdatedCartResponse response = applyCouponService.applyCoupon(id, cart);
        return ResponseEntity.ok(response);
    }
}
