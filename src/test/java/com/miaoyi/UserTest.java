package com.miaoyi;

import com.miaoyi.jpa.entity.User;
import com.miaoyi.jpa.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@Slf4j
public class UserTest extends MiaoyiApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void userTest() {
        User user = new User();
        user.setUsername("admin");
        user.setName("管理员");
        user.setPassword("sb123.++");
        userRepository.save(user);

        User row = userRepository.findByUsername("admin");
        System.out.println(row);
    }
}
