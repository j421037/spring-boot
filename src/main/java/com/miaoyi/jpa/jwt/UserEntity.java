/**
 * @author ：wangxin
 * @date ：Created in 2020-04-01 16:39
 * @modified By： none
 */
package com.miaoyi.jpa.jwt;

import com.miaoyi.jpa.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

public class UserEntity implements UserDetails {
    public UserEntity(User user) {
        username = user.getUsername();
        password = user.getPassword();
    }

    private String username;

    private String password;

    private Collection<? extends GrantedAuthority> role;

    private Collection<? extends GrantedAuthority> authorities;

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public void setRole(String role) {
        this.role = AuthorityUtils.commaSeparatedStringToAuthorityList(role);
    }

    public Collection<? extends GrantedAuthority> getRole() {
        return role;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<GrantedAuthority> authorities)
    {
        this.authorities = authorities;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }
}