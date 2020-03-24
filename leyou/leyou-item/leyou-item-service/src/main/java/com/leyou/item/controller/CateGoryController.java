package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-10 13:52
 */
@Controller
@RequestMapping("category")
public class CateGoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询分类
     * @param pid
     * @return
     *
     */
    @GetMapping("list")

    public ResponseEntity<List<Category>> queryCategoriesByPid (@RequestParam(value = "pid",defaultValue = "0") Long pid){

        if(pid == null || pid < 0){
            return ResponseEntity.badRequest().build();
        }
        List<Category> categoryList = categoryService.queryCategoriesByPid(pid);
        if(CollectionUtils.isEmpty(categoryList)){
            return ResponseEntity.notFound().build();
        }

        return  ResponseEntity.ok(categoryList);

    }

    /**
     *根据商品id查询分类
     * @param pid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId (@PathVariable("bid") Long pid) {

        List<Category> categoryList = categoryService.queryByBrandId(pid);

        if (CollectionUtils.isEmpty(categoryList)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categoryList);
    }

    @GetMapping
    public ResponseEntity<List<String>> queryNamesByIds (@RequestParam("ids") List<Long> ids) {

        List<String> names = categoryService.qureyNameByIds(ids);

        if (CollectionUtils.isEmpty(names)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
    }
}

