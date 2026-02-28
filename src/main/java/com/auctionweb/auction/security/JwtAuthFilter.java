package com.auctionweb.auction.security;

import com.auctionweb.auction.service.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter — runs on every HTTP request.
 * Reads the Authorization header, validates the JWT, and sets the user as authenticated.
 */
public class JwtAuthFilter extends OncePerRequestFilter {  // No @Component — created manually in SecurityConfig

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {  // Constructor injection
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Read the Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. If no header or doesn't start with "Bearer ", skip JWT check
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (remove the "Bearer " prefix)
        String token = authHeader.substring(7);

        // 4. Validate the token
        if (jwtUtil.isTokenValid(token)) {
            String email = jwtUtil.extractEmail(token);

            // 5. Tell Spring Security: this user is authenticated
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Spring Security 6: must create a new context and set it explicitly
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }

        // 6. Always pass the request along to the next filter / controller
        filterChain.doFilter(request, response);
    }
}
