package com.example.plugin.examples;

import com.example.plugin.IPlugin;

public class DemoPlugin implements IPlugin {
    @Override
    public void execute() {
        System.out.println("DemoPlugin version " + getVersion() + " is executing...");
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
} 