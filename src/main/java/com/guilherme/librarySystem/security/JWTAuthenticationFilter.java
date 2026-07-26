package com.guilherme.librarySystem.security;


import com.guilherme.librarySystem.exceptions.GlobalExceptionHandler;
import com.guilherme.librarySystem.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;

//handles the /login request
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // JsonMapper (Jackson 3) instead of ObjectMapper (Jackson 2): this project runs on
    // Spring Boot 4.1.0, whose default Jackson is 3.x, under the "tools.jackson" package.
    // JsonMapper is immutable/thread-safe, so a single shared instance is fine to reuse.
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private AuthenticationManager authenticationManager;

    private JWTUtil jwtUtil;

    //constructor
    public JWTAuthenticationFilter(AuthenticationManager authenticationManager
            , JWTUtil jwtUtil) {
        setAuthenticationFailureHandler(new GlobalExceptionHandler());
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    //attempt to validate the email and the password
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            User userCredentials = JSON_MAPPER.readValue(request.getInputStream(), User.class);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userCredentials.getEmail(), userCredentials.getPassword(), new ArrayList<>());

            Authentication authentication = authenticationManager.authenticate(authToken);
            return authentication;
        } catch (IOException e){ //if reading the request body fails
            throw new RuntimeException(e);
        }
    }

    @Override //if authentication succeeds, this runs:
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain,
                                            Authentication authentication) throws IOException, ServletException {
        UserSpringSecurity userSpringSecurity = (UserSpringSecurity) authentication.getPrincipal();
        String email = userSpringSecurity.getUsername();
        String token = jwtUtil.generateToken(email);
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
    } //this returns the token to the user, to be used to authenticate on other routes

}
