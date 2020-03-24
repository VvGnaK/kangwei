package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-10 14:01
 */
@Service
public class CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父id查询子节点
     * @param pid
     * @return
     */
    public List<Category> queryCategoriesByPid(Long pid) {

        Category record = new Category();
        record.setParentId(pid);
        return categoryMapper.select(record);

    }

    /**
     * 根据商品id查询分类 修改商品
     * @param pid
     * @return
     */
    public List<Category> queryByBrandId(Long pid) {

        return categoryMapper.queryByBrandId(pid);

    }

    /**
     * 查询分类名称根据 ids
     * @param ids
     * @return
     */
    public List<String> qureyNameByIds(List<Long> ids) {

        List<Category> list = categoryMapper.selectByIdList(ids);
        List<String> names = new ArrayList<>();
        for(Category category : list) {
            names.add(category.getName());
        }

        return names;

        // return list.stream().map(category -> category.getName()).collect(Collectors.toList());
    }

}
