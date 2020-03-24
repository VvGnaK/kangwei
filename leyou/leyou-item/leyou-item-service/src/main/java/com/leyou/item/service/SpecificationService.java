package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-10 17:56
 */
@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {

        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return specGroupMapper.select(specGroup);

    }

    /**
     * 查询规格参数
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    public List<SpecParam> queryParamByGid(Long gid, Long cid, Boolean generic, Boolean searching) {

        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);

        return specParamMapper.select(specParam);

    }

    /**
     * 查询规格组，同时在规格组内的所有参数。
     * @param id
     * @return
     */
    public List<SpecGroup> queryGroupWithParam(Long id) {
        List<SpecGroup> groups = queryGroupsByCid(id);
        groups.forEach(group -> {
            List<SpecParam> params = queryParamByGid(group.getId(), null, null, null);
            group.setParams(params);
        });


        return groups;
    }
}
