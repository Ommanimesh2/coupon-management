package com.monk.coupon.dto.request;

import com.monk.coupon.enums.CouponType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddCouponRequest {

    @NotNull
    private CouponType type;

    @Valid
    @NotNull
    private CouponDetailsRequest details;
}
