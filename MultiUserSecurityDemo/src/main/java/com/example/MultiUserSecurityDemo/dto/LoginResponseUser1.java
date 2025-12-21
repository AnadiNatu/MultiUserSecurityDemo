package com.example.MultiUserSecurityDemo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseUser1 {

    private String token;
    private String username;
    private String userType;
    private String role;
    private String message;

}
