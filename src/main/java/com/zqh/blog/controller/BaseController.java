package com.zqh.blog.controller;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/23 0023 下午 11:22
* description: controller层基类
**/
public abstract class BaseController {
    public String ret404Page() {
        return "pub/404";
    }
}
