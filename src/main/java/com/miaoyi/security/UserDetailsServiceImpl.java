/**
 * @author ：wangxin
 * @date ：Created in 2020-04-01 16:22
 * @modified By： none
 * 负责从数据库读取用户信息
 */
package com.miaoyi.security;

import com.miaoyi.jpa.entity.User;
import com.miaoyi.jpa.jwt.UserEntity;
import com.miaoyi.jpa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);


        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        UserEntity userEntity = new UserEntity(user);
        userEntity.setRole("ROLE_ADMIN");

        return new org.springframework.security.core.userdetails.User(userEntity.getUsername(), userEntity.getPassword(), userEntity.getRole());
    }
}