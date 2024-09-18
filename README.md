# Coupon Management API

## Objective

The goal of this API is to manage and apply different types of discount coupons for an e-commerce platform. The API supports multiple coupon types, such as cart-wise, product-wise, and buy-x-get-y (BxGy) (specifically, BnG1). The system is designed to be extensible, allowing the addition of new coupon types in the future.

## Coupon Types

1. **Cart-wise Coupons**: Discounts applied based on the total value of the cart.
2. **Product-wise Coupons**: Discounts applied to specific products in the cart.
3. **Buy-X-Get-Y (BxGy) Coupons**: Customers receive free products when they buy specific quantities of other products.

## Use Cases

### Cart-wise Coupons

- **Threshold-based discount**: If the total value of the cart exceeds a specified amount, a percentage discount is applied.
  - **Example**: 10% off on carts worth more than $100.

### Product-wise Coupons

- **Product-specific discount**: A percentage or fixed discount is applied to specific products in the cart.
  - **Example**: 20% off on product_id 1.

### BxGy Coupons

- **Buy-X-Get-Y scenario**: Customers can get certain products for free when they purchase a specified number of other products.
  - **Example**: Buy 2 of Product A and get 1 of Product B for free.

## Assumptions for proper funcitoning

1.  A cart can only apply one coupon at a time. (Able to enforce this from the UI side. Else we can also include check in the backend. (currently not implemented))
2.  BxGy coupons can be applied if any product from the cart matches with the quantity mentioned in the coupon. For example:-

    - If the cart has 6 units of A and 8 units of B, and the coupon requires 2 units of A and 4 units of B, and gives 2 units of C for free,
      The coupon can be applied:

           3 times for A (6/2 = 3)
           2 times for B (8/4 = 2)
           So the total number of times the coupon can be applied is 3 + 2 = 5 times.

           This number is then matched with the repetitionLimit and whichever is minimum is taken as the maxNoOfTimesCouponApplicable.

    - Then it checks the buyProducts array from the couopn and checks if there are applicable products present, if present, iteratively applies coupons to them till the maxNoOfTimesCouponApplicable becomes 0 and returns the total discount. The discounted products will see their whole amount to be mentioned with the discount in cart..
      For example

      ```
        {
            "productId": 4,
            "quantity": 2,
            "price": 400.0,
            "totalDiscount": 800.0
        }
      ```

    - The system will track quantities and conditions based on product IDs, which must match exactly between the cart and the coupon.

## Limitations

1. **Coupon stacking rules**: Currently, the API does not allow stacking multiple coupons on the same cart item, but this could be a future enhancement. Though it is allowed, it'll lead to compounding of discounts which is not desired.
1. **BxGy limitations**:

- The API supports B2G1 coupons completely but has scope of improvement in the B2G2 or similar classes. The issue being currently, the discount gets applied to all the items in the getProducts list.
- Another problem with them being that it is currently not able to disscern various products and apply coupon after taking into account prices of the products.
- For example
  If I have 2 products in my cart like so
  ```
   {
            "productId": 1,
            "quantity": 6,
            "price": 100
        },
        {
            "productId": 2,
            "quantity": 3,
            "price": 200
        },
  ```
- And my coupon is being applied, then it according to the benefit of the consumer, it should initially be applied to the costliest product and then after that the next costlier product. In a descending order of cost.
- This cost is the cost of a single quantity of that item not the cost of quantity \* singlePrice.
- Currently this is not implemented.

## Future Enhancements

1. **Additional Coupon Types**: The system should easily accommodate new coupon types (e.g., B2G2, time-limited coupons, user-specific coupons).
2. **Coupon Validation Enhancements**: Extend the validation system to cover more complex conditions such as user eligibility, expiration time, or product categories.
3. **Unit Tests**: The tests can be written to ensure proper funtioning without need to do manual API testing
4. **Bulk Discounts**: Implement a bulk discount coupon that provides a discount when a customer buys a large quantity of a product (e.g., 10% off for buying 10 or more items).

## Key Code Features

- **Cart-wise coupon calculation**: Iterates through all cart items to determine if the cart meets the total value threshold for the discount.
- **Product-wise coupon calculation**: Applies a discount to specific products in the cart if they match the conditions of the coupon.
- **BxGy coupon calculation**: Determines how many times a customer meets the conditions for getting free products and applies the appropriate discount.

## How to Extend

1. To add new coupon types:
   - Define a new coupon type in the `CouponType` enum.
   - Ensure that it extends the abstract class of CouponDetails.
   - Define your entity logic
   - Create a new method for calculating the discount in the service layer.
   - Ensure that the new coupon logic is integrated into the coupon application switch-case in `applyCoupon`.
2. Consider implementing logging, validations, and error handling for the new coupon types.

## Example API Endpoints

1. **Apply Coupon**: `POST /coupons/apply-coupon/{id}`

   - Applies a coupon to a given cart and returns the updated cart with discounts applied.

2. **List Coupons**: `GET /coupons`

   - Lists all available coupons, including the types and conditions.

3. **Create Coupon**: `POST /coupons`

   - Creates a new coupon with details about the discount type and conditions.

4. **Get Coupon By ID**: `GET /coupons/{id}`

   - Retrieves a coupon by its ID.

5. **Update Coupon**: `PUT /coupons/{id}`

   - Updates a coupon's details.

6. **Delete Coupon**: `DELETE /coupons/{id}`

   - Deletes a coupon by its ID.

7. **Get Applicable Coupons**: `POST /coupons/applicable-coupons`
   - Returns a list of coupons applicable to the current cart.

Sample API collection of postman is attached for easy reference
