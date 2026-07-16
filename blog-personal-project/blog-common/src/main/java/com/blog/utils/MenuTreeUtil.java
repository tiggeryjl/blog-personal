package com.blog.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MenuTreeUtil {

    // 缓存类对应的三个字段，避免重复反射解析
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new HashMap<>();

    public static <T> List<T> buildTree(List<T> list) {
        return buildTree(list, 0L);
    }

    public static <T> List<T> buildTree(List<T> list, Long rootParentId) {
        if (list == null || list.isEmpty()) return new ArrayList<>();

        Set<Long> allMenuIds = list.stream()
                .map(item -> getId(item))
                .collect(Collectors.toSet());

        List<T> rootList = list.stream()
                .filter(item -> {
                    Long pid = getParentId(item);
                    return pid.equals(rootParentId) || !allMenuIds.contains(pid);
                })
                .collect(Collectors.toList());

        // 没有根节点，说明是孤立叶子，全部作为根返回
        if(rootList.isEmpty()){
            rootList = new ArrayList<>(list);
        }

        rootList.forEach(node -> setChildren(node, getChildNodes(node, list)));
        return rootList;
    }

    private static <T> List<T> getChildNodes(T parent, List<T> allList) {
        Long parentId = getId(parent);
        List<T> children = allList.stream()
                .filter(item -> getParentId(item).equals(parentId))
                .collect(Collectors.toList());
        children.forEach(child -> setChildren(child, getChildNodes(child, allList)));
        return children;
    }

    // 缓存获取当前类的三个Field：id、parentId、children
    private static <T> Field[] getCachedFields(T obj) {
        Class<?> clazz = obj.getClass();
        return FIELD_CACHE.computeIfAbsent(clazz, k -> {
            try {
                Field idField = clazz.getDeclaredField("id");
                Field pidField = clazz.getDeclaredField("parentId");
                Field childField = clazz.getDeclaredField("children");
                idField.setAccessible(true);
                pidField.setAccessible(true);
                childField.setAccessible(true);
                return new Field[]{idField, pidField, childField};
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("实体必须包含 id、parentId、children 三个属性", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Long getId(T obj) {
        Field[] fields = getCachedFields(obj);
        try {
            return (Long) fields[0].get(obj);
        } catch (Exception e) {
            log.info("获取权限id失败", e);
            throw new RuntimeException("获取权限id失败");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Long getParentId(T obj) {
        Field[] fields = getCachedFields(obj);
        try {
            return (Long) fields[1].get(obj);
        } catch (Exception e) {
            log.info("获取parentId失败", e);
            throw new RuntimeException("获取parentId失败");
        }
    }

    private static <T> void setChildren(T obj, List<T> children) {
        Field[] fields = getCachedFields(obj);
        try {
            fields[2].set(obj, children);
        } catch (Exception e) {
            log.info("设置children失败", e);
            throw new RuntimeException("设置children失败");
        }
    }
}