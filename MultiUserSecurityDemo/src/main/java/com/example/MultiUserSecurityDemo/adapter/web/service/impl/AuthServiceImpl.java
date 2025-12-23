package com.example.MultiUserSecurityDemo.adapter.web.service.impl;

import com.example.MultiUserSecurityDemo.adapter.web.dto.*;
import com.example.MultiUserSecurityDemo.adapter.web.service.AuthService;
import com.example.MultiUserSecurityDemo.domain.model.*;
import com.example.MultiUserSecurityDemo.domain.port.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserType1Port userType1Port;
    private final UserType2Port userType2Port;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserType1Port userType1Port ,UserType2Port userType2Port ,PasswordEncoder passwordEncoder){
        this.userType1Port = userType1Port;
        this.userType2Port = userType2Port;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SignUpResponse signup(SignUpRequest request) {
        if (emailExists(request.getEmail())){
            return SignUpResponse.builder()
                    .message("Email already exists")
                    .build();
        }

        if ("TYPE1".equalsIgnoreCase(request.getUserType())){
            return registerUserType1(request);
        }else if ("TYPE2".equalsIgnoreCase(request.getUserType())){
            return registerUserType2(request);
        }else {
            return SignUpResponse.builder()
                    .message("Invalid user type. Use TYPE1 or TYPE2")
                    .build();
        }
    }

    public boolean emailExists(String email){
        return userType1Port.findByEmail(email).isPresent() ||
                userType2Port.findByEmail(email).isPresent();
    }

    public SignUpResponse registerUserType1(SignUpRequest request){
        try{
            UserType1 user = new UserType1();
            user.setFname(request.getFname());
            user.setLname(request.getLname());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());

            UserRoles1 role = parseUserRole1(request.getRole());
            user.setRoles1(role);

            UserType1 savedUser = userType1Port.save(user);
            return SignUpResponse.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .fname(savedUser.getFname())
                    .lname(savedUser.getLname())
                    .userType("TYPE1")
                    .role(savedUser.getRoles1().name())
                    .message("User of TYPE1 registered successfully")
                    .build();
        }catch (Exception ex){
            return SignUpResponse.builder()
                    .message("Registration failed: " + ex.getMessage())
                    .build();
        }
    }

    private SignUpResponse registerUserType2(SignUpRequest request){
        try{
            UserType2 user = new UserType2();
            user.setFname(request.getFname());
            user.setLname(request.getLname());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());

            UserRoles2 role = parseUserRoles2(request.getRole());
            user.setRole(role);

            UserType2 savedUser = userType2Port.save(user);
            return SignUpResponse.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .fname(savedUser.getFname())
                    .lname(savedUser.getLname())
                    .userType("TYPE2")
                    .role(savedUser.getRole().name())
                    .message("User registered successfully")
                    .build();
        }catch (Exception ex){
            return SignUpResponse.builder()
                    .message("Registration failed: " + ex.getMessage())
                    .build();
        }
    }

    private UserRoles1 parseUserRole1(String roleString){
        if (roleString == null || roleString.isEmpty()){
            return UserRoles1.ADMIN_TYPE2;
        }
        try{
            return UserRoles1.valueOf(roleString.toUpperCase());
        }catch (IllegalArgumentException ex){
            return UserRoles1.ADMIN_TYPE2;
        }
    }

    private UserRoles2 parseUserRoles2(String roleString){
        if (roleString == null || roleString.isEmpty()){
            return UserRoles2.USER_TYPE2;
        }
        try{
            return UserRoles2.valueOf(roleString.toUpperCase());
        }catch (IllegalArgumentException ex){
            return UserRoles2.USER_TYPE2;
        }
    }
}
