package com.miaoyi.controller;

import com.miaoyi.jpa.entity.User;
import com.miaoyi.jpa.repository.UserRepository;
import com.miaoyi.response.HttpResponse;
import com.miaoyi.validator.auth.AuthLoginValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;


@RestController
@RequestMapping(value = "/admin")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "hello world122";
    }

    @PostMapping("/login")
    public HttpResponse login(@Valid AuthLoginValidator request) {
        HttpResponse response;

        User user = userRepository.findByUsername(request.getUsername());

        if (user.getPassword().equals(DigestUtils.sha1Hex(request.getPassword()))) {
            Map<String, User> result = new HashMap<String, User>() {{ put("row", user); }};
            response = HttpResponse.success(result);
        }
        else {
            response = HttpResponse.error("账号密码错误");
        }

        return response;
    }


    @GetMapping("/users")
    public Map<String, List<User>> users() {
        List<User> users = userRepository.findAll();

        return new HashMap<String, List<User>>(){{ put("rows", users); }};
    }
}
