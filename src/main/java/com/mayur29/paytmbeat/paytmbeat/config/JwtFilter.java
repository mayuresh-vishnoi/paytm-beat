package com.mayur29.paytmbeat.paytmbeat.config;

import com.mayur29.paytmbeat.paytmbeat.service.UserDetailsImpl;
import com.mayur29.paytmbeat.paytmbeat.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        String jwt = "";
        String username = "";

        if (authorization!= null && authorization.startsWith("Bearer ")){
            jwt = authorization.substring(7);
            username = jwtUtils.extractUserName(jwt);
        }

        if (username!=null && !username.isEmpty()){
            UserDetails userDetails = userDetailsImpl.loadUserByUsername(username);
            System.out.println("userDetails ::"+userDetails);
            if (!jwtUtils.validateToken(jwt)){
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails,null,userDetails.getAuthorities()
                );
                System.out.println("here");
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request,response);
    }
}
