package com.spring.bean;

/**
 * @author wazh
 * @description
 * @since 2023-02-20-21:14
 */
public interface ApplicationContext {

    Object getBean(Class<?> clazz);
}
