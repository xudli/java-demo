package com.github.xdli.container;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.xdli.annotation.Autowired;
import com.github.xdli.annotation.Component;
import com.github.xdli.exception.BeanException;

public class BeanContainer {
    private Map<String, Object> beans = new HashMap<>();
    private ClassScanner scanner = new ClassScanner();

    public void init(String basePackage) throws Exception {
        // 扫描指定包下的所有类
        Set<Class<?>> classes = scanner.scan(basePackage);
        
        // 首先创建所有bean实例
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Component component = clazz.getAnnotation(Component.class);
                String beanName = component.value().isEmpty() ? 
                    toLowerFirstCase(clazz.getSimpleName()) : component.value();
                Object instance = clazz.getDeclaredConstructor().newInstance();
                beans.put(beanName, instance);
            }
        }
        
        // 注入依赖
        for (Object bean : beans.values()) {
            injectDependencies(bean);
        }
    }

    private void injectDependencies(Object bean) throws Exception {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                String beanName = toLowerFirstCase(field.getType().getSimpleName());
                Object dependencyBean = beans.get(beanName);
                if (dependencyBean == null) {
                    throw new BeanException("找不到类型为 " + field.getType() + " 的Bean");
                }
                field.set(bean, dependencyBean);
            }
        }
    }

    public Object getBean(String name) {
        return beans.get(name);
    }

    private String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
} 