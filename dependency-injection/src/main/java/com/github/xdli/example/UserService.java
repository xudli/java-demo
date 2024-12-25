package com.github.xdli.example;

import com.github.xdli.annotation.Component;

@Component
public class UserService {
    public void sayHello() {
        System.out.println("Hello from UserService");
    }
} 