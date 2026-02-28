package com.auctionweb.auction.service;

import com.auctionweb.auction.dto.LoginRequest;
import com.auctionweb.auction.dto.RegisterRequest;
import com.auctionweb.auction.dto.UserResponse;
import com.auctionweb.auction.model.User;
import com.auctionweb.auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Register a new user with email + password
     */
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setIsAdmin(false);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    /**
     * Login user with email + password
     */
    public UserResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Google-only users have no password
        if (user.getPasswordHash() == null) {
            throw new RuntimeException("This account uses Google login. Please sign in with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return mapToResponse(user);
    }

    /**
     * Login (or auto-register) a user via Google OAuth.
     *
     * Steps:
     *  1. Verify the Google token by calling Google's tokeninfo endpoint
     *  2. Check the token was issued for OUR app (clientId check)
     *  3. Find existing user by email, or create a new one
     *  4. Return our own JWT
     */
    @SuppressWarnings("unchecked")
    public UserResponse loginWithGoogle(String googleToken) {
        // Step 1: Ask Google to verify the token
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleToken;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.getForEntity(url, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Google token");
        }

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to verify Google token");
        }

        Map<String, Object> googleData = response.getBody();

        // Step 2: Make sure the token was issued for YOUR Google app, not someone else's
        String audience = (String) googleData.get("aud");
        if (!googleClientId.equals(audience)) {
            throw new RuntimeException("Token was not issued for this application");
        }

        // Step 3: Extract user info from the Google token
        String email     = (String) googleData.get("email");
        String fullName  = (String) googleData.get("name");
        String avatarUrl = (String) googleData.get("picture");
        String googleId  = (String) googleData.get("sub"); // Google's unique user ID

        // Step 4: Find existing user or create a new one
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get(); // Returning user — just log them in
        } else {
            // New user — create an account automatically
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            user.setGoogleId(googleId);
            user.setIsAdmin(false);
            // No passwordHash — this user logs in only via Google

            // Auto-generate a unique username from their email (e.g. john@gmail.com → john)
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }
            user.setUsername(username);
            user = userRepository.save(user);
        }

        return mapToResponse(user);
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return mapToResponse(userOpt.get());
    }

    /**
     * Convert a User entity into a UserResponse (safe to send to frontend).
     * Generates a real JWT token here.
     */
    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setAvatarUrl(user.getAvatarUrl());
        // Generate a real signed JWT token
        response.setToken(jwtUtil.generateToken(user.getEmail(), user.getId().toString()));
        return response;
    }
}
