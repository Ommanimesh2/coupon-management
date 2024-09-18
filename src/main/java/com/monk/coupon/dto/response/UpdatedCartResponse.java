package com.monk.coupon.dto.response;

import java.util.List;

import com.monk.coupon.dto.request.CartItemDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedCartResponse {
    private List<CartItemDto> items;
    private double totalPrice;
    private double totalDiscount;
    private double finalPrice;
}