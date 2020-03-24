package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**品牌查询
 * @author VvGnaK
 * @date 2020-03-10 14:24
 */
@Controller
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {

        PageResult<Brand> pageResult = brandService.queryBrandsByPage(key,page,rows,sortBy,desc);
        if (CollectionUtils.isEmpty(pageResult.getItems())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(pageResult);
    }


    /**
     * 新增品牌
     * json对象在前端转换为json字符串了 所以直接用 brand，ids来直接接收
     * @param brand
     * @param ids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addBrand(Brand brand, @RequestParam("cids") List<Long> ids) {

        brandService.addBrand(brand,ids);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 商品列表新增  根据cid 回显品牌名称
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid (@PathVariable("cid") Long cid) {

        List<Brand> brandList = brandService.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(brandList)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brandList);
    }

    /**
     * 查询品牌表 15839151529
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById (@PathVariable Long id) {
        Brand brand = brandService.queryBrandById(id);
        if (brand == null){
            ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }

}
