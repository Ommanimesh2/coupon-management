package com.monk.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.monk.coupon.model.coupon.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
}