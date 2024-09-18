package com.monk.coupon.dto.response;

import java.util.List;

import com.monk.coupon.dto.request.ApplicableCouponDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableCouponsResponse {
    private List<ApplicableCouponDto> applicableCoupons;
}