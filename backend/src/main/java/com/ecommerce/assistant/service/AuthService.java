package com.ecommerce.assistant.service;

import com.ecommerce.assistant.dto.*;
import com.ecommerce.assistant.entity.Role;
import com.ecommerce.assistant.entity.ShoppingCart;
import com.ecommerce.assistant.entity.User;
import com.ecommerce.assistant.entity.UserPreference;
import com.ecommerce.assistant.exception.BadRequestException;
import com.ecommerce.assistant.exception.ConflictException;
import com.ecommerce.assistant.exception.ResourceNotFoundException;
import com.ecommerce.assistant.exception.UnauthorizedException;
import com.ecommerce.assistant.repository.RoleRepository;
import com.ecommerce.assistant.repository.ShoppingCartRepository;
import com.ecommerce.assistant.repository.UserPreferenceRepository;
import com.ecommerce.assistant.repository.UserRepository;
import com.ecommerce.assistant.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final ShoppingCartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserPreferenceRepository preferenceRepository,
        ShoppingCartRepository cartRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.preferenceRepository = preferenceRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsActive(true);

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
            .orElseGet(() -> roleRepository.save(new Role("ROLE_CUSTOMER")));
        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);

        // Initialize user preferences and shopping cart
        UserPreference preference = new UserPreference(savedUser);
        preferenceRepository.save(preference);

        ShoppingCart cart = new ShoppingCart(savedUser);
        cartRepository.save(cart);

        List<String> roleNames = savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), roleNames);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return new AuthResponse(accessToken, refreshToken, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), roleNames);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }

        List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getFullName(), roleNames);
    }

    public AuthResponse refreshToken(TokenRefreshRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(), user.getFullName(), roleNames);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        preferenceRepository.findByUserId(userId).ifPresent(pref -> {
            response.setMaxBudget(pref.getMaxBudget());
            response.setPreferredCategories(parseJsonList(pref.getPreferredCategories()));
            response.setPreferredBrands(parseJsonList(pref.getPreferredBrands()));
            response.setFavoriteFeatures(parseJsonList(pref.getFavoriteFeatures()));
        });

        return response;
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            String clean = json.replace("[", "").replace("]", "").replace("\"", "").trim();
            if (clean.isBlank()) return Collections.emptyList();
            List<String> list = new ArrayList<>();
            for (String item : clean.split(",")) {
                list.add(item.trim());
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
