package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


/**
 * @author VvGnaK
 * @date 2020-03-10 14:25
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 查询品牌并分页
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        //2  添加分页条件
        PageHelper.startPage(page,rows);

        // 添加排序条件
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        List<Brand> brandList = brandMapper.selectByExample(example);

        PageInfo<Brand> brandPageInfo = new PageInfo<>(brandList);

        return new PageResult<>(brandPageInfo.getTotal(),brandPageInfo.getList());

    }

    /**
     * 品牌新增
     * @param brand
     * @param ids
     */
    @Transactional
    public void addBrand(Brand brand, List<Long> ids) {

        brandMapper.insertSelective(brand);

        ids.forEach(id -> {
            brandMapper.insertCategoryAndBrand(id,brand.getId());
        });

    }

    /**
     * 商品列表新增  根据cid 回显品牌名称
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {

       return brandMapper.selectBrandByCid(cid);
    }


    /**
     *根据spu表中的brandId查询品牌
     * @param id
     * @return
     */
    public Brand queryBrandById(Long id) {

        return brandMapper.selectByPrimaryKey(id);
    }
}
