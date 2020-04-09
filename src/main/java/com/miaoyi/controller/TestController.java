/**
 * @author ：wangxin
 * @date ：Created in 2020-04-02 09:37
 * @modified By： none
 */
package com.miaoyi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
public class TestController {
    @GetMapping("/")
    public String index() {
        return "content";
    }
}