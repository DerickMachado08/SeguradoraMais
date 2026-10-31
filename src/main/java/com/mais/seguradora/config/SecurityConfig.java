package com.mais.seguradora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Páginas públicas
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login",
                                "/login.html",

                                "/senha/**",

                                "/css/**",
                                "/js/**",
                                "/images/**",

                                "/cadastro-cliente.html",
                                "/cadastro-corretor.html"
                        ).permitAll()

                        // Todo o resto também permitido por enquanto
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form.disable())

                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
