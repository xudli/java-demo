package com.github.xdli.example;

import com.github.xdli.annotation.Autowired;
import com.github.xdli.annotation.Component;

@Component
public class UserController {
    @Autowired
    private UserService userService;
    
    public void doSomething() {
        userService.sayHello();
    }
} 