package com.example.MultiUserSecurityDemo.adapter.web.controller;


import com.example.MultiUserSecurityDemo.adapter.security.security_files.JwtUtil;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;
import com.example.MultiUserSecurityDemo.adapter.web.dto.LoginRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.LoginResponse;
import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpRequest;
import com.example.MultiUserSecurityDemo.adapter.web.dto.SignUpResponse;
import com.example.MultiUserSecurityDemo.adapter.web.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager , JwtUtil jwtUtil , AuthService authService){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@RequestBody SignUpRequest request){
        if (request.getEmail() == null || request.getEmail().isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(SignUpResponse.builder().message("Email is required").build());
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(SignUpResponse.builder().message("Password is required").build());
        }

        if (request.getUserType() == null || request.getUserType().isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(SignUpResponse.builder().message("User Type is Required (TYPE1 or TYPE2)").build());
        }

        SignUpResponse response = authService.signup(request);
        if (response.getId() != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){

        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername() , loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String token = jwtUtil.generateToken(userDetails);
            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("UNKNOW");

            String userType;
            if (userDetails instanceof UserType1Details){
                userType="TYPE1";
            }else if (userDetails instanceof UserType2Details){
                userType="TYPE2";
            }else{
                userType="UNKNOW";
            }

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .username(userDetails.getUsername())
                    .userType(userType)
                    .role(role)
                    .message("Login Successful")
                    .build();

            return ResponseEntity.ok(response);
        }catch (Exception ex){
            LoginResponse response = LoginResponse.builder()
                    .token(null)
                    .username(null)
                    .userType(null)
                    .role(null)
                    .message("Invalid credentials: " + ex.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Auth endpoint working!");
    }
}


