package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author VvGnaK
 * @date 2020-03-16 9:47
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
