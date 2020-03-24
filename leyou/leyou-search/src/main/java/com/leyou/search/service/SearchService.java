package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;

import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.reponsitory.GoodsReponsitory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

;

@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsReponsitory goodsReponsitory;


    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     *把查询到的Spu转变为Goods来保存
     * @param spu
     * @return
     * @throws IOException
     */
    public Goods buildGoods(Spu spu) throws IOException {

        Goods goods = new Goods();
        //获取品牌对象 拿到品牌名称brand.getName  15839151529
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        //查询分类名称
        List<String> names = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //查询spu下的所有sku
        List<Sku> skuList = goodsClient.querySkuById(spu.getId());
        List<Long> prices = new ArrayList<>();
        List<Map<String, Object>> mapList = new ArrayList<>();
        skuList.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(), ",") : "");
            mapList.add(skuMap);
        });

        //获取所有规格参数
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), null, true);
        //查询spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());
        //spuDetail中的generic反序列化
        Map<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });
        //特殊规格参数
        Map<Long, Object> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, Object>>() {
        });

        //定义map接收
        Map<String, Object> paramMap = new HashMap<>();

        params.forEach(param -> {
            //判断是否是通用参数
            if (param.getGeneric()) {
                //通用规格参数
                String value = genericSpecMap.get(param.getId()).toString();
                //判断是否是数值类型
                if (param.getNumeric()) {
                    //如果是数值 判断该数值落在哪个区间
                    chooseSegment(value, param);
                }
                paramMap.put(param.getName(), value);
            } else {
                paramMap.put(param.getName(), specialSpecMap.get(param.getId()));
            }

        });


        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //过去spu下的所有sku的价格
        goods.setPrice(prices);
        //拼接all字段 标题 分类 品牌
        goods.setAll(spu.getTitle() + " " + brand.getName() + " " + StringUtils.join(names, " "));
        //获取spu下的所有sku，并转化为json字符串
        goods.setSkus(MAPPER.writeValueAsString(mapList));
        //获取所有规格参数
        goods.setSpecs(paramMap);
        return goods;
    }

    /**
     * 因为过滤参数中有一类比较特殊，就是数值区间
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


//    public PageResult<Goods> search(SearchRequest request) {
//        String key = request.getKey();
//        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
//        if (StringUtils.isBlank(key)) {
//            return null;
//        }
//
//        // 构建查询条件
//        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//
//        // 1、对key进行全文检索查询
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
//
//        // 2、通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
//        queryBuilder.withSourceFilter(new FetchSourceFilter(
//                new String[]{"id","skus","subTitle"}, null));
//
//        // 3、分页
//        // 准备分页参数
//        int page = request.getPage();
//        int size = request.getSize();
//        queryBuilder.withPageable(PageRequest.of(page - 1, size));
//
//        // 4、查询，获取结果
//        Page<Goods> goodsPage = this.goodsReponsitory.search(queryBuilder.build());
//
//        // 封装结果并返回
//        return new PageResult<>(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent());
//
//    }
public com.leyou.search.pojo.SearchResult search(SearchRequest request) {
    String key = request.getKey();
    // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
    if (StringUtils.isBlank(key)) {
        return null;
    }

    // 构建查询条件
    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

    // 1、对key进行全文检索查询
    //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
    BoolQueryBuilder basicQuery = buildBooleanQueryBuilder(request);
    queryBuilder.withQuery(basicQuery);

    // 2、通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
    queryBuilder.withSourceFilter(new FetchSourceFilter(
            new String[]{"id","skus","subTitle"}, null));

    // 3、分页
    // 准备分页参数
    int page = request.getPage();
    int size = request.getSize();
    queryBuilder.withPageable(PageRequest.of(page - 1, size));

    String categoryAggName = "categories";
    String brandAggName = "brands";
    queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
    queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

    // 4、查询，获取结果
    AggregatedPage<Goods> goodsPage  = (AggregatedPage<Goods>) this.goodsReponsitory.search(queryBuilder.build());

    // 解析聚合结果集
    List<Map<String, Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
    List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));
    //判断分类集合的结果 等于1聚合
    List<Map<String, Object>> specs = null;
    if (!CollectionUtils.isEmpty(categories) && categories.size() == 1) {
        specs = getParamAggResult((Long)categories.get(0).get("id"),basicQuery);
    }


    // 封装结果并返回
    return new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent(),categories,brands,specs);
}


    /**
     * 构建bool条件查询 构建器
     * @param request
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest request) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));

        // 添加过滤条件

        if (CollectionUtils.isEmpty(request.getFilter())){
            return boolQueryBuilder;
        }
        for (Map.Entry<String, Object> entry : request.getFilter().entrySet()) {

            String key = entry.getKey();
            // 如果过滤条件是“品牌”, 过滤的字段名：brandId
            if (StringUtils.equals("品牌", key)) {
                key = "brandId";
            } else if (StringUtils.equals("分类", key)) {
                // 如果是“分类”，过滤字段名：cid3
                key = "cid3";
            } else {
                // 如果是规格参数名，过滤字段名：specs.key.keyword
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }

        return boolQueryBuilder;
    }
    /**
     * 根据查询条件集合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        //创建自定义查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(basicQuery);
        //查询聚合得规格参数
        List<SpecParam> specParams = specificationClient.queryParams(null, cid, null, true);
        //添加规格参数的聚合
        specParams.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });

        // 只需要聚合结果集，不需要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        // 执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsReponsitory.search(queryBuilder.build());

        // 定义一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();
        // 解析聚合查询的结果集
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            // 放入规格参数名
            map.put("k", entry.getKey());
            // 收集规格参数值
            List<Object> options = new ArrayList<>();
            // 解析每个聚合
            StringTerms terms = (StringTerms)entry.getValue();
            // 遍历每个聚合中桶，把桶中key放入收集规格参数的集合中
            terms.getBuckets().forEach(bucket -> options.add(bucket.getKeyAsString()));
            map.put("options", options);
            paramMapList.add(map);
        }

        return paramMapList;
    }
    /**
     * 解析品牌聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        // 处理聚合结果集
        LongTerms terms = (LongTerms)aggregation;
        // 获取所有的品牌id桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        // 定义一个品牌集合，搜集所有的品牌对象
        List<Brand> brands = new ArrayList<>();
        // 解析所有的id桶，查询品牌
        buckets.forEach(bucket -> {
            Brand brand = this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
        return brands;
    }

    /**
     * 解析分类
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {

        // 处理聚合结果集
        LongTerms terms = (LongTerms)aggregation;
        // 获取所有的分类id桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        // 定义一个品牌集合，搜集所有的品牌对象
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = new ArrayList<>();
        // 解析所有的id桶，查询品牌
        buckets.forEach(bucket -> {
            cids.add(bucket.getKeyAsNumber().longValue());
        });
        List<String> names = this.categoryClient.queryNameByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }
        return categories;

    }

    public void save(Long id) throws IOException {
        Spu spu = goodsClient.querySpuById(id);
        Goods goods = buildGoods(spu);
        // 保存数据到索引库
        this.goodsReponsitory.save(goods);
    }

    public void delete(Long id) {
        goodsReponsitory.deleteById(id);
    }
}
