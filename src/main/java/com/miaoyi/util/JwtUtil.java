package com.miaoyi.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.miaoyi.exception.JwtTokenRefreshException;
import javafx.util.Pair;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCache;
import org.springframework.security.core.userdetails.User;

import java.util.Date;
public class JwtUtil {

    public final static String TOKEN_HEADER = "Authorization";

    public final static String AUTH_LOGIN_URL = "/admin/login";

    private final static long EXPIRE_TIME = 6 * 1000;


    /**
     * 生成token 2小时过期
     * @param username 用户名
     * @param password 密码
     * **/
    public static Pair<String, Date> sign(String username, String password) {
         Date expireDate = getExpireAt();

        try {
            Algorithm algorithm = Algorithm.HMAC256(password);
            String token = JWT.create().withClaim("username", username).withExpiresAt(getExpireAt()).sign(algorithm);

            return new Pair<>(token, expireDate);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查token是否正确
     * @param
     * **/
    public static boolean verify(String username, String password, String token) throws TokenExpiredException {
        Algorithm algorithm = Algorithm.HMAC256(password);
        JWTVerifier jwtVerifier = JWT.require(algorithm).withClaim("username", username).build();
        DecodedJWT jwt = jwtVerifier.verify(token);
        return true;
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     * @return token中包含的用户名
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);

            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * token 过期十分钟内可获得一个新的token
     * **/
    public static String refreshToken(String token, CacheManager cacheManager) throws JwtTokenRefreshException {
        try {

            DecodedJWT jwt = JWT.decode(token);

            String username = jwt.getClaim("username").asString();

            Date expire = jwt.getExpiresAt();

            Long time = expire.getTime();

            Long now = System.currentTimeMillis();

            JCacheCache jwtCache = getCache(cacheManager, "jwt-cache");

            JCacheCache tokenCache = getCache(cacheManager, "token-cache");

            User user = (User) jwtCache.get(username).get();

            Object _token = tokenCache.get(token);

            //从token缓存中移除登陆信息
            clearUserInCache(getCache(cacheManager), token);

            // 过期10分钟内 返回一个新的token
            if ((now - time) < 10 * 60 * 1000 && _token != null) {
                Pair<String, Date> sign = sign(username, user.getPassword());
                putTokenToCache(sign, getCache(cacheManager));

                return sign.getKey();
            }
            // 已过期
            throw new JwtTokenRefreshException();
        }
        catch (NullPointerException exception) {
            throw new JwtTokenRefreshException();
        }

    }

    /**
     * 获取缓存
     * **/
    public static JCacheCache getCache(CacheManager cacheManager) {
        return (JCacheCache) cacheManager.getCache("token-cache");
    }

    /**
     * 获取缓存
     * **/
    public static JCacheCache getCache(CacheManager cacheManager, String cacheName) {
        return (JCacheCache) cacheManager.getCache(cacheName);
    }

    public static Date getExpireAt() {
        return new Date(System.currentTimeMillis() + EXPIRE_TIME);
    }

    /**
     * 从缓存中移除用户登陆信息
     * **/
    public static void clearUserInCache(JCacheCache cache, String token) {
        try {
            cache.evict(token);
        }
        catch (NullPointerException exception) {

        }
    }

    /**
     * 更新用户登陆信息
     * **/
    public static void putTokenToCache(Pair<String, Date> sign, JCacheCache tokenCache) {

        try {
            if (tokenCache.get(sign.getKey()) != null) {
                // cache.remove(username);
                tokenCache.evict(sign.getKey());
            }

            // 将token 保存在cache中
            tokenCache.put(sign.getKey(), sign.getValue());
        }
        catch (NullPointerException exception) {
            return;
        }

    }

}
