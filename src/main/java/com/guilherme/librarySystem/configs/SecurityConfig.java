package com.guilherme.librarySystem.configs;

import com.guilherme.librarySystem.security.JWTAuthenticationFilter;
import com.guilherme.librarySystem.security.JWTAuthorizationFilter;
import com.guilherme.librarySystem.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    private static final String[] PUBLIC_MATCHERS_POST = { //user and login are public for POST
            "/user",
            "/login"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { //filterChain receives the http request
        http.csrf(csrf -> csrf.disable()) //disable csrf protection
                // CORS is handled by WebConfig's WebMvcConfigurer.addCorsMappings, not here —
                // keeping only one CORS configuration in the project instead of two conflicting ones.
                .cors(cors -> cors.disable());

        AuthenticationManagerBuilder authenticationManagerBuilder = http //add to the AuthenticationManager builder
                .getSharedObject(AuthenticationManagerBuilder.class); //with password encryption enabled
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder());
        this.authenticationManager = authenticationManagerBuilder.build();

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, PUBLIC_MATCHERS_POST).permitAll()
                .anyRequest().authenticated()).authenticationManager(authenticationManager);

        http.addFilter(new JWTAuthenticationFilter(authenticationManager, jwtUtil));
        http.addFilter(new JWTAuthorizationFilter(authenticationManager, jwtUtil, userDetailsService));

        http.sessionManagement(session -> session //no session is kept
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() { //to encrypt
        return new BCryptPasswordEncoder();
    }
}
