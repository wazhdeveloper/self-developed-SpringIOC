package com.spring.bean.Impl;

import com.spring.annotation.Bean;
import com.spring.annotation.DI;
import com.spring.bean.ApplicationContext;
import org.springframework.context.annotation.Description;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//用于手动实现ioc中，依赖注入的原理与过程
public class AnnotationApplicationContext implements ApplicationContext {
    private final Map<Class<?>, Object> beanFactory = new HashMap<>();
    private String rootPath;

    @Description("主要用来返回ioc容器中的对象")
    @Override
    public Object getBean(Class<?> clazz) {
        return beanFactory.get(clazz);
    }

    //用来根据包的路径，扫描包下所有添加@Bean注解的类，将类添加到ioc容器中
    public AnnotationApplicationContext(String basePackage) {
        String decode = null;
        try {
            //将形参传进来的地址修改成路径写法
            String s = basePackage.replaceAll("\\.", "\\\\");
            //获取包的全路径
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(s);
            //扫描包下是否还有子包或文件
            if (urls.hasMoreElements()) {
                //获取子包或文件
                URL url = urls.nextElement();
                //转码
                decode = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                rootPath = decode.substring(0, decode.length() - s.length());
                loadBean(new File(decode));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadDi();
    }

    //属性注入
    private void loadDi() {
        try {
            Set<Map.Entry<Class<?>, Object>> entries = beanFactory.entrySet();
            //遍历ioc容器
            for (Map.Entry<Class<?>, Object> entry : entries) {
                //得到容器中的实例
                Object value = entry.getValue();
                Class<?> clazz = value.getClass();
                //获得实例的属性
                Field[] fields = clazz.getDeclaredFields();
                if (fields.length > 0) {
                    for (Field field : fields) {
                        //属性是否有DI注解
                        DI di = field.getAnnotation(DI.class);
                        if (di != null) {
                            field.setAccessible(true);
                            //如果有注解，为属性赋值
                            field.set(value, beanFactory.get(field.getType()));//意思为：为field对象中的field属性赋值为beanFactory.get(field.getType())
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加载类到ioc
    private void loadBean(File file) {
        try {
            //1.判断当前路径是否是文件夹
            if (file.isDirectory()) {
                //如果是文件夹就扫描文件夹下是否有文件
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    //2.如果没有就返回
                    return;
                } else {
                    //3.如果有子包或文件，遍历
                    for (File childFile : files) {
                        //4.如果是子包，就递归
                        if (childFile.isDirectory()) {
                            loadBean(childFile);
                        } else {
                            //5.如果是文件，获取文件全路径
                            String absolutePath = childFile.getAbsolutePath();
                            //6.1截取全路径的结尾部分
                            String substring = absolutePath.substring(rootPath.length() - 1);
                            //6.文件以.class结尾
                            if (substring.endsWith(".class")) {
                                //替换路径中/为.
                                //6.2删去.class
                                String s = substring.replaceAll("\\\\", "\\.").replaceAll(".class", "");
                                //6.3反射
                                Class<?> clazz = Class.forName(s);
                                //6.3.0判断是不是接口
                                if (clazz.isInterface()) return;
                                //6.3.1是否有@Bean注解
                                Bean bean = clazz.getAnnotation(Bean.class);
                                if (bean != null) {
                                    //6.3.2如果有注解，实例化
                                    Object instance = clazz.getConstructor().newInstance();
                                    //6.3.3如果有接口，容器中以接口为key
                                    Class<?>[] interfaces = clazz.getInterfaces();
                                    if (interfaces.length > 0) {
                                        beanFactory.put(interfaces[0], instance);
                                    } else {
                                        beanFactory.put(clazz, instance);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
