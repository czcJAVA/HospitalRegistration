package com.itmk.config.security.filter;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.itmk.config.security.detailservice.CustomerUserDetailService;
import com.itmk.config.security.exception.CustomerAuthenionException;
import com.itmk.config.security.handler.LoginFailureHandler;
import com.itmk.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Author java实战基地
 * @Version 2383404558
 * 验证思路：登录和验证码不需要验证
 */
@Data
@Component("CheckTokenFilter")
public class CheckTokenFilter extends OncePerRequestFilter {
    @Value("#{'${ignore.url}'.split(',')}")
    private List<String> ignoreUrl = Collections.emptyList();

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private CustomerUserDetailService customerUserDetailService;
    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException {
        try{
            //获取请求的url
            String uri = request.getRequestURI();
            //请求不在配置的白名单，验证token
            if(!ignoreUrl.contains(uri) && !uri.contains("/images/") && !uri.startsWith("/wxapi/allApi/")){
                //token验证
                validateToken(request);
            }
        }catch (AuthenticationException e){
            loginFailureHandler.commence(request,response,e);
            return;
        }
        filterChain.doFilter(request,response);
    }
    protected void validateToken(HttpServletRequest request){
        //从请求头部获取token
        String token = request.getHeader("token");
        //如果请求头部token为空，从参数获取
        if(StringUtils.isEmpty(token)){
            token = request.getParameter("token");
        }
        if(StringUtils.isEmpty(token)){
            throw new CustomerAuthenionException("请传递token！");
        }
        //token验证
        if(!jwtUtils.verify(token)){
            throw new CustomerAuthenionException("非法的token！");
        }
        //解析token
        DecodedJWT decodedJWT = jwtUtils.jwtDecode(token);
        Map<String, Claim> claims = decodedJWT.getClaims();
        //登录账户
        String username = claims.get("username").asString();
        //用户认证
        UserDetails details = customerUserDetailService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(details,
                null,details.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        //设置到springsecurity的上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
