package com.monk.coupon.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDetailsRequest {

    @Positive(message = "Threshold must be greater than 0 ")
    private Double threshold;

    @Positive(message = "discount must be greater than 0")
    private Double discount;

    @Positive(message = "product_id must be greater than 0")
    private Long product_id;

    private List<ProductDto> buyProducts;

    private List<ProductDto> getProducts;

    @Min(value = 1, message = "Repetition limit must be at least 1")
    private Integer repetitionLimit;
}
