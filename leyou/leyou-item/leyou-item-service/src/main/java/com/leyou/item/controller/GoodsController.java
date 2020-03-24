package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-10 19:01
 */
@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage (
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows

    ) {

        PageResult<SpuBo> pageResult = goodsService.querySpuBoByPage(key,saleable,page,rows);

        if (CollectionUtils.isEmpty(pageResult.getItems())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 商品列表新增
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods (@RequestBody SpuBo spuBo) {

        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 商品修改回显 查询 skuDdetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId (@PathVariable("spuId") Long spuId) {
        SpuDetail spuDetail = goodsService.querySpuDetailBySpuId(spuId);

        if (spuDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 商品修改回显,查询sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuById (@RequestParam("id") Long spuId) {

        List<Sku> skuList = goodsService.querySkuById(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skuList);
    }

    /**
     * 商品列表更新
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods (@RequestBody SpuBo spuBo) {

        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById (@PathVariable Long id) {

        Spu spu = goodsService.querySpuById(id);
        if (spu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 根据skuId查询sku
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId") Long skuId) {

        Sku sku = goodsService.querySkuBySkuId(skuId);

        if (sku == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }

}
