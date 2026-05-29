package com.iteaj.iboot.module.core.service.impl;

import com.iteaj.framework.spi.excel.IExcelService;
import com.iteaj.iboot.module.core.entity.Org;
import com.iteaj.iboot.module.core.service.IDictDataService;
import com.iteaj.iboot.module.core.service.IOrgService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelServiceImpl implements IExcelService {

    private final IOrgService orgService;
    private final IDictDataService dictDataService;
    private final static String ORG_CACHE = "ORG:CACHE";
    private final static String DICT_CACHE_TO_NAME = "DICT:CACHE:TO:VALUE";
    private final static String DICT_CACHE_TO_VALUE = "DICT:CACHE:TO:VALUE";

    public ExcelServiceImpl(IOrgService orgService, IDictDataService dictDataService) {
        this.orgService = orgService;
        this.dictDataService = dictDataService;
    }

    @Override
    public String toDictName(String dictType, Serializable dictValue) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object attribute = attributes.getAttribute(DICT_CACHE_TO_NAME, RequestAttributes.SCOPE_REQUEST);
        if(attribute instanceof Map) { // 从请求对象里面获取数据
            Map<Serializable, String> stringMap = ((Map<String, Map<Serializable, String>>) attribute).get(dictType);
            return stringMap != null ? stringMap.get(dictValue) : null;
        } else { // 将数据写入到请求对象
            Map<String, Map<Serializable, String>> map = new HashMap<>();
            dictDataService.list().forEach(item -> {
                Map<Serializable, String> stringMap = map.get(item.getType());
                if(stringMap == null) {
                    map.put(item.getType(), stringMap = new HashMap<>());
                }

                stringMap.put(item.getValue(), item.getLabel());
            });

            attributes.setAttribute(DICT_CACHE_TO_NAME, map, RequestAttributes.SCOPE_REQUEST);
            Map<Serializable, String> stringMap = map.get(dictType);
            return stringMap != null ? stringMap.get(dictValue) : null;
        }
    }

    @Override
    public Serializable toDictValue(String dictType, String dictName) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object attribute = attributes.getAttribute(DICT_CACHE_TO_VALUE, RequestAttributes.SCOPE_REQUEST);
        if(attribute instanceof Map) { // 从请求对象里面获取数据
            Map<String, Serializable> stringMap = ((Map<String, Map<String, Serializable>>) attribute).get(dictType);
            return stringMap != null ? stringMap.get(dictName) : null;
        } else { // 将数据写入到请求对象
            Map<String, Map<String, Serializable>> map = new HashMap<>();
            dictDataService.list().forEach(item -> {
                Map<String, Serializable> stringMap = map.get(item.getType());
                if(stringMap == null) {
                    map.put(item.getType(), stringMap = new HashMap<>());
                }

                stringMap.put(item.getLabel(), item.getValue());
            });

            attributes.setAttribute(DICT_CACHE_TO_VALUE, map, RequestAttributes.SCOPE_REQUEST);
            Map<String, Serializable> stringMap = map.get(dictType);
            return stringMap != null ? stringMap.get(dictName) : null;
        }
    }

    @Override
    public String toOrgName(Serializable id) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object attribute = attributes.getAttribute(ORG_CACHE, RequestAttributes.SCOPE_REQUEST);
        if(attribute instanceof List) { // 从请求对象里面获取数据
            Org org = ((List<Org>) attribute).stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst().get();
            return org != null ? org.getName() : null;
        } else { // 将数据写入到请求对象
            List<Org> data = orgService.list()
                    .ofNullable().orElse(Collections.emptyList());
            attributes.setAttribute(ORG_CACHE, data, RequestAttributes.SCOPE_REQUEST);
            Org org = data.stream().filter(item -> item.getId().equals(id)).findFirst().get();
            return org != null ? org.getName() : null;
        }
    }

    @Override
    public Serializable toOrgValue(String name) {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        Object attribute = attributes.getAttribute(ORG_CACHE, RequestAttributes.SCOPE_REQUEST);
        if(attribute instanceof List) { // 从请求对象里面获取数据
            Org org = ((List<Org>) attribute).stream()
                    .filter(item -> item.getName().equals(name))
                    .findFirst().get();

            return org != null ? org.getId() : null;
        } else { // 将数据写入到请求对象
            List<Org> data = orgService.list()
                    .ofNullable().orElse(Collections.emptyList());
            attributes.setAttribute(ORG_CACHE, data, RequestAttributes.SCOPE_REQUEST);
            Org org = data.stream()
                    .filter(item -> item.getName().equals(name))
                    .findFirst().get();

            return org != null ? org.getId() : null;
        }
    }
}
