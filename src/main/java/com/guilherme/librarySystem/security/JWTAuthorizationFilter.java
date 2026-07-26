package com.guilherme.librarySystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.util.Objects;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private JWTUtil jwtUtil;

    private UserDetailsService userDetailsService;

    //constructor
    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
                                  UserDetailsService userDetailsService) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String authorizationHeader = request.getHeader("Authorization");
        if (Objects.nonNull(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); //starts with "Bearer ", but "Bearer " isn't part of the token
            UsernamePasswordAuthenticationToken auth = getAuthentication(token);
            if (Objects.nonNull(auth)) {
                SecurityContextHolder.getContext().setAuthentication(auth); //tells Spring Security who's making this request
            }
        }
        chain.doFilter(request, response); //always continue the filter chain, with or without a token
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        if(this.jwtUtil.isValidToken(token)) { //to authenticate the token, if the token is valid:
            String email = this.jwtUtil.getEmail(token);
            UserDetails user = this.userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authenticatedUser = new UsernamePasswordAuthenticationToken(user, null,
                    user.getAuthorities());
            return authenticatedUser; //uses the token's data to build authentication from the token itself,
        }               //checking whether it's valid and extracting the username to look up the user,
        return null;    //returning what the rest of the filter chain needs
    }

}