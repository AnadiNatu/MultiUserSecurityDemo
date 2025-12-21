package com.example.MultiUserSecurityDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserType1Repository user1Repository;

    @Autowired
    private UserType2Repository user2Repository;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@RequestBody SignUpRequest request) {

        // Check if user already exists
        if (authService.hasUserWithEmail(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .body("User already exists");
        }
        Object userDTO;
        // Decide user type
        if (usernameSortFunction().equalsIgnoreCase(request.getUserType())) {
            userDTO = authService.signupUserType1(request);
        } else {
            userDTO = authService.signupUserType2(request);
        }
        // Validate creation
        if (userDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("User not created");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDTO);
}

    @PostMapping("/loginUser1")
    public ResponseEntity<LoginResponseUser1> login(@RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(loginRequest.getUsername());

            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            LoginResponseUser1 response = LoginResponseUser1.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .userType(jwtUtil.getUserType(token))
                    .role(role)
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            LoginResponseUser1 response = LoginResponseUser1.builder()
                    .token(null)
                    .username(null)
                    .userType(null)
                    .role(null)
                    .message("Invalid credentials: " + ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/loginUser2")
    public ResponseEntity<LoginResponseUser2> login(@RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(loginRequest.getUsername());

            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            LoginResponseUser1 response = LoginResponseUser1.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .userType(jwtUtil.getUserType(token))
                    .role(role)
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            LoginResponseUser2 response = LoginResponseUser2.builder()
                    .token(null)
                    .username(null)
                    .userType(null)
                    .role(null)
                    .message("Invalid credentials: " + ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
