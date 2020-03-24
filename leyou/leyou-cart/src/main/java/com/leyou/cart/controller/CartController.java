package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-16 9:31
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 增加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {

        cartService.addCart(cart);
        return ResponseEntity.ok().build();

    }

    /**
     * 查询登录状态的购物车
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCartList() {

        List<Cart> carts = cartService.queryCartList();
        if (CollectionUtils.isEmpty(carts)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 增加或者减少购物车数量
     * @param cart
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart) {

        cartService.updateNum(cart);
        return ResponseEntity.noContent().build();

    }

    /**
     * 删除购物车
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
