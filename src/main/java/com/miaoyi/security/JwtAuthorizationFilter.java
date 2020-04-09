/**
 * @author ：wangxin
 * @date ：Created in 2020-04-02 16:54
 * @modified By： none
 */
package com.miaoyi.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaoyi.exception.JwtTokenRefreshException;
import com.miaoyi.response.HttpResponse;
import com.miaoyi.util.JwtUtil;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private CacheManager cacheManager;

    private UserDetailsService userService;

    public JwtAuthorizationFilter(UserDetailsService userService, CacheManager cacheManager) {
        this.userService = userService;
        this.cacheManager = cacheManager;
    }

    // 重写实际进行过滤操作的doFilterInternal方法
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        String token = null;
        // 获取请求uri
        String uri = request.getRequestURI();

        // 如果不是后台的入口 则直接 放行
        if (!uri.matches("/admin/.*")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 尝试获取请求头中的token
            String header = request.getHeader(JwtUtil.TOKEN_HEADER);


            if (header.isEmpty()) {
                return;
            }

            if (header.startsWith("Bearer ")) {
                token = header.split(" ")[1];
            }

            if (!token.isEmpty()) {
                Authentication authentication = getAuthentication(token);

                if (authentication != null) {

                    // 认证成功
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    chain.doFilter(request, response);
                }
            }


        }
        catch (NullPointerException exception) {
            // token不存在、认证失败 返回401
            response.setStatus(401);
        }
        catch (TokenExpiredException exception) {
            try {
                // 刷新token
                String refreshToken = JwtUtil.refreshToken(token, cacheManager);

                // token 刷新成功
                if (!refreshToken.isEmpty()) {
                    Authentication authentication = getAuthentication(refreshToken);
                    response.addHeader(JwtUtil.TOKEN_HEADER, String.format("Bearer %s", refreshToken));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    chain.doFilter(request, response);
                }

            }
            catch (JwtTokenRefreshException e) {
                // token 过期
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> res = new HashMap<String, String>() {{
                    put("state", "error");
                }};
                response.setStatus(468);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(mapper.writeValueAsString(HttpResponse.error("登陆已失效")));
            }
        }
    }

    /**
     * 验证token
     * **/
    private UsernamePasswordAuthenticationToken getAuthentication(String token)
        throws TokenExpiredException
    {
        String username = JwtUtil.getUsername(token);
        UserDetails userDetails = null;

        try {
            userDetails = userService.loadUserByUsername(username);
        }
        catch (UsernameNotFoundException exception) {
            return null;
        }

        boolean state = JwtUtil.verify(username, userDetails.getPassword(), token);

        if (state) {
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }

        return null;
    }


}