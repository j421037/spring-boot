/**
 * @author ：wangxin
 * @date ：Created in 2020-04-01 17:03
 * @modified By： none
 */
package com.miaoyi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoyi.response.HttpResponse;
import com.miaoyi.util.JwtUtil;
import javafx.util.Pair;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
    过滤器一定要设置 AuthenticationManager，所以此处我们这么编写，这里的 AuthenticationManager
    从 Security 配置的时候传入
    */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private CacheManager cacheManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, CacheManager cacheManager) {
        this.authenticationManager = authenticationManager;
        this.cacheManager = cacheManager;
        // 设置登陆的url
        setFilterProcessesUrl(JwtUtil.AUTH_LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 从请求中得到账号密码
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        setDetails(request, token);

        // 交给AuthenticationManager 鉴权
        return authenticationManager.authenticate(token);
    }

    /**
     * 鉴权成功
     * **/
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException
    {
        handle(request, response, authResult, null);
    }

    /**
     * 鉴权失败
     * **/
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException
    {
        handle(request, response, null, failed);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, Authentication authResult, AuthenticationException failed)
        throws IOException, ServletException
    {
        // ObjectMapper 将对象转换为json jackson
        ObjectMapper mapper = new ObjectMapper();
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());

        if (authResult != null) {
            // 登陆成功
            UserDetails user = (UserDetails) authResult.getPrincipal();
            Pair<String, Date> token = JwtUtil.sign(user.getUsername(), user.getPassword());

            Map<String, String> data = new HashMap<String, String>() {{ put("token", String.format("Bearer %s", token.getKey())); }};

            JwtUtil.putTokenToCache(token, JwtUtil.getCache(cacheManager));

            response.getWriter().write(mapper.writeValueAsString(HttpResponse.success(data)));
        }
        else {
            // 登陆失败
            response.getWriter().write(mapper.writeValueAsString(HttpResponse.error("用户名或密码错误")));
        }
    }

}