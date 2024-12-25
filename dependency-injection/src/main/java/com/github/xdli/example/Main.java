package com.github.xdli.example;

import com.github.xdli.container.BeanContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        BeanContainer container = new BeanContainer();
        container.init("com.github.xdli.example");
        
        UserController controller = (UserController) container.getBean("userController");
        controller.doSomething();
    }
} 