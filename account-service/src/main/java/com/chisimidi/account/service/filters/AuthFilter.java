package com.chisimidi.account.service.filters;

import com.chisimidi.account.service.services.JwtUtilService;
import com.chisimidi.account.service.utils.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilService jwtUtilService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization=request.getHeader("Authorization");
        if(authorization!=null&&authorization.startsWith("Bearer ")){
            String token =authorization.substring(7);
            if(jwtUtilService.isTokenValid(token)){
                String userName= jwtUtilService.extractUserName(token);
                String role= jwtUtilService.extractRole(token);
                int userId= jwtUtilService.extractUserId(token);
                CustomUserPrincipal customUserPrincipal=new CustomUserPrincipal(userName,role,userId);
                UsernamePasswordAuthenticationToken auth =new UsernamePasswordAuthenticationToken(customUserPrincipal,null, List.of(new SimpleGrantedAuthority("ROLE_"+role)));
                SecurityContext securityContext=new SecurityContextImpl();
                securityContext.setAuthentication(auth);
                SecurityContextHolder.setContext(securityContext);
            }
        }
        filterChain.doFilter(request,response);
    }

}
