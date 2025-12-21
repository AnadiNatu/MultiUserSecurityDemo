package com.example.MultiUserSecurityDemo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseUser2 {

    private String token;
    private String username;
    private String userType;
    private String role;
    private String message;

}
