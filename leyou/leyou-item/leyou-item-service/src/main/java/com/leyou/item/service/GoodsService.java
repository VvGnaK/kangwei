package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-10 19:03
 */
@Service
public class GoodsService {

    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 根据条件查询商品列表 通过spuBo继承 组合实体类  查询分类名称用的ids集合
     * 通用mapper 需要继承  SelectByIdListMapper<Category, Long>
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, boolean saleable, Integer page, Integer rows) {

        //添加分页条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //添加上下架过滤条件
        if (saleable == false || true) {
            criteria.andEqualTo("saleable", saleable);
        }


        //分页条件

        PageHelper.startPage(page, rows);
        //执行条件
        List<Spu> spus = spuMapper.selectByExample(example);

        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        List<SpuBo> spuBoList = new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);

            List<String> names = categoryService.qureyNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            spuBo.setCname(StringUtils.join(names, "--"));

            spuBo.setBname(brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBoList.add(spuBo);

        });

        return new PageResult<>(pageInfo.getTotal(), spuBoList);
    }


    /**
     * 商品列表新增
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {

        //添加spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insertSelective(spuBo);


        // 新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

//        //添加spuDetail
//        SpuDetail spuDetail = new SpuDetail();
//        spuDetail.setSpuId(spuBo.getId());
//        spuDetailMapper.insertSelective(spuDetail);
//
        saveSkuAndStock(spuBo);

        sendMessage(spuBo.getId(),"insert");
    }
    private void saveSkuAndStock(SpuBo spuBo) {
        //添加sku
        spuBo.getSkus().forEach(sku -> {
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            skuMapper.insertSelective(sku);

            //添加stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockMapper.insertSelective(stock);

        });
    }


    /**
     * 查询SkuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {

        return spuDetailMapper.selectByPrimaryKey(spuId);

    }

    /**
     * 查询sku
     * @param spuId
     * @return
     */
    public List<Sku> querySkuById(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);

        skuList.forEach(sku1 -> {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            sku1.setStock(stock.getStock());
        });

        return skuList;
    }


    /**.
     * 商品列表修改
     * @param spuBo
     */
    public void updateGoods(SpuBo spuBo) {
        //删除Stock表的所有数据
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skuList = skuMapper.select(record);

        skuList.forEach(sku -> {
            //删除stock
            stockMapper.deleteByPrimaryKey(sku.getId());
        });
        //删除Sku和库存
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        skuMapper.delete(sku);
        //新增sku
        saveSkuAndStock(spuBo);

        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spuBo);

        //更新spu详情

        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMessage(spuBo.getId(),"update");


    }

    public Spu querySpuById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * rmq 消息发送
     * @param id
     * @param type
     */
    private void sendMessage(Long id, String type){
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据SkuId查询sku
     * @param skuId
     * @return
     */
    public Sku querySkuBySkuId(Long skuId) {

        return skuMapper.selectByPrimaryKey(skuId);
    }
}
