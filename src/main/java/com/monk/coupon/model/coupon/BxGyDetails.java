package com.monk.coupon.model.coupon;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.monk.coupon.model.BxGyProductQuantity;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BxGyDetails extends CouponDetails {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bxgy_buy_id")
    private List<BxGyProductQuantity> buyProducts;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bxgy_get_id")
    private List<BxGyProductQuantity> getProducts;

    private int repetitionLimit;
}