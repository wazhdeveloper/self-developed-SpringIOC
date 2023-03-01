package com.spring.service.Impl;

import com.spring.annotation.Bean;
import com.spring.annotation.DI;
import com.spring.dao.UserDao;
import com.spring.service.UserService;

@Bean
public class UserServiceImpl implements UserService {

    @DI
    private UserDao userDao;

    @Override
    public void add() {
        userDao.say();
        System.out.println("add~~~~");
    }
}
