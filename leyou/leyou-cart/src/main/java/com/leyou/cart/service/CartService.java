package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VvGnaK
 * @date 2020-03-16 9:31
 */
@Service
public class CartService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    static final String KEY_PREFIX = "leyou:cart:uid:";

    /**
     * 增加购物车
     * @param cart
     */
    public void addCart(Cart cart) {

        //获取登录用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //redis的key
        String key = KEY_PREFIX + userInfo.getId();
        //获取hash操作对象（双层map中的map）

        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);

        //查询是否存在
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        Boolean boo = boundHashOps.hasKey(skuId.toString());

        if (boo) {
            //如果存在
            String cartJson = boundHashOps.get(skuId.toString()).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum() + num);

        }else {
            //如果不存在
            cart.setUserId(userInfo.getId());

            Sku sku = goodsClient.querySkuBySkuId(skuId);
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
        }
        // 将购物车数据写入redis
        boundHashOps.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));

    }

    /**
     * 查询登录章台的购物车
     * @return
     */
    public List<Cart> queryCartList() {
        //获取用户信息

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 判断是否存在购物车
        String key = KEY_PREFIX + userInfo.getId();
        if (!this.redisTemplate.hasKey(key)) {
            return null;
        }
        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);
        List<Object> carts = boundHashOps.values();
        // 判断是否有数据
        if(CollectionUtils.isEmpty(carts)){
            return null;
        }

        // 查询购物车数据
        return carts.stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());


    }

    /**
     * 购物车添加或减少
     * @param cart
     */
    public void updateNum(Cart cart) {

        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + userInfo.getId();
        if (!redisTemplate.hasKey(key)) {
            return;
        }

        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);

        String cartJson = boundHashOps.get(cart.getSkuId().toString()).toString();

        Cart cart1 = JsonUtils.parse(cartJson, Cart.class);

        cart1.setNum(cart.getNum());

        boundHashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart1));


    }

    /**
     * 删除购物车
     * @param skuId
     */
    public void deleteCart(String skuId) {

        // 获取登录用户
        UserInfo user = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + user.getId();

        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);

        boundHashOps.delete(skuId);
    }
}
