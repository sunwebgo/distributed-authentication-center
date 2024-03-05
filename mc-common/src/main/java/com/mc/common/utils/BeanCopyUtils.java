package com.mc.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xu huaiang
 * @description: BeanCopy工具类
 * @date 2023/10/19
 */
public class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    /**
     * 单个bean拷贝
     * @param source
     * @param clazz
     * @return {@link V}
     */
    public static <V> V copyBean(Object source, Class<V> clazz) {
        //创建目标对象（V 创建的对象即使传过来的对象）
        V result = null;
        try {
            result = clazz.newInstance();//利用反射
            //实现属性copy
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回结果
        return result;
    }

    /**
     * 拷贝整个集合
     * @param list
     * @param clazz
     * @return {@link List}<{@link V}>
     */
    public static <O, V> List<V> copyBeanList(List<O> list, Class<V> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }
}
