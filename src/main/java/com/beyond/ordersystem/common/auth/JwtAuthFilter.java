package com.beyond.ordersystem.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.security.sasl.AuthenticationException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends GenericFilter {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String bearerToken = ((HttpServletRequest) request).getHeader("Authorization");
        try {
            if (bearerToken != null) {
                // 토큰 있으면 처리, 없으면 알아서 에러
                // token 관례적으로 Bearer로 시작하는 문구를 넣어서 요청
                if (!bearerToken.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String token = bearerToken.substring(7);
                // token 검증 및 claims(사용자 정보) 추출
                // token 생성시에 사용한 secret 키 값을 넣어 토큰 검증에 사용
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody(); //getBody 는 payload 에 들어있는 것.

                //Authentication 객체 생성 - 네모 안에 네모 안에 있던 거. . . -> 스프링 전역에서 사용 가능함.
                // => UserDetails 객체가 필요하다. 그래야 member_id 대신에 다른 값을 넣을 수 있도록 가져올 수 있으니까 !
                List<GrantedAuthority> authorities = new ArrayList<>();// User 에 들어갈 권한인 List 객체.
                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities); // getSubject 안에 사용자 정보 들어있다. 사용자의 비밀번호는 알 수 없고 토큰에 안 넣을 거라서 안 들어있음.
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }
        catch (Exception e){
            log.error(e.getMessage());
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("token-error");
        }


    }

}

