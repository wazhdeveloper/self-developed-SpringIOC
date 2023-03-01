package com.spring.dao.Impl;

import com.spring.annotation.Bean;
import com.spring.dao.UserDao;

@Bean
public class UserDaoImpl implements UserDao {

    @Override
    public void say() {
        System.out.println("the di annotation has been created success!");
    }
}
