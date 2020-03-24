package com.leyou.search.reponsitory;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface GoodsReponsitory extends ElasticsearchRepository<Goods,Long> {
}
