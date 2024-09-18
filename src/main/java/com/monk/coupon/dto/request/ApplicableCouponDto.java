package com.monk.coupon.dto.request;

import com.monk.coupon.enums.CouponType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApplicableCouponDto {
    private Long couponId;
    private CouponType type;
    private double discount;
}