/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * BMS Spring Security 配置（form-login 页面 + JWT API 双轨）。
 *
 * <p>路径策略：</p>
 * <table>
 *   <tr><td>/bms/login.do (GET/POST)</td>          <td>放行（form-login URL，本身就是登录入口）</td></tr>
 *   <tr><td>/statics/** /favicon.*  / 错误页</td>  <td>放行</td></tr>
 *   <tr><td>/bms/api/auth/login</td>               <td>放行（POST 拿 JWT）</td></tr>
 *   <tr><td>/bms/api/**</td>                       <td>需要 JWT 鉴权</td></tr>
 *   <tr><td>/bms/user/**</td>                      <td>需要 ADMIN 角色</td></tr>
 *   <tr><td>/bms/**（其他）</td>                   <td>需要登录</td></tr>
 * </table>
 *
 * <p>CSRF 默认关闭以方便 ajax/post，可按需打开（restful api 走 JWT 不依赖 cookie 故可禁）。</p>
 *
 * @author 钟启辉
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private LoginSuccessHandler      loginSuccessHandler;
    @Autowired private JwtAuthFilter            jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 10 与种子数据生成时一致
        return new BCryptPasswordEncoder(10);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF：form-login 不依赖 token；JWT 接口本身在 header
            .csrf().disable()
            // 接口统一 JSON 响应：未登录的 API 路径返回 401 + BaseResponse，而不是重定向到登录页
            .exceptionHandling()
                .defaultAuthenticationEntryPointFor(
                        (req, resp, ex) -> {
                            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                            resp.setContentType("application/json;charset=UTF-8");
                            // 与 ResultCodeEnum.UNAUTHORIZED 保持一致（阿里 A0230：未登录或登录已过期）
                            resp.getWriter().write(
                                    "{\"code\":\"A0230\",\"message\":\"未登录或 token 无效\"}");
                        },
                        new AntPathRequestMatcher("/bms/api/**"))
            .and()

            .authorizeRequests()
                // 登录入口与静态资源
                .antMatchers("/bms/login.do",
                             "/bms/api/auth/login",
                             "/statics/**", "/favicon.ico", "/favicon",
                             "/error", "/pub/**").permitAll()
                // AMS 业务路径（如果误打到本进程，例如本机 8081 直接访问 /，放行让 Spring 决定 404）
                .antMatchers("/", "/index", "/index.html").permitAll()
                // 用户/角色管理仅 ADMIN
                .antMatchers("/bms/user/**", "/bms/api/user/**").hasRole("ADMIN")
                // 其他 BMS 路径需登录
                .antMatchers("/bms/**").authenticated()
                .anyRequest().permitAll()
            .and()

            // 表单登录：复用既有的 /bms/login.do 路径与 adminLogin.html 模板
            .formLogin()
                .loginPage("/bms/login.do")
                .loginProcessingUrl("/bms/login.do")
                .usernameParameter("userName")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/bms/login.do?error=1")
                .permitAll()
            .and()

            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/bms/logout"))
                .logoutSuccessUrl("/bms/login.do?logout=1")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            .and()

            .sessionManagement()
                .maximumSessions(1) // 同账号同时只能在一处登录
                .expiredUrl("/bms/login.do?expired=1");

        // 把 JWT filter 插在 UsernamePasswordAuthenticationFilter 之前，让 API 请求先尝试 JWT
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
