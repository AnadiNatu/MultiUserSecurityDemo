package com.example.MultiUserSecurityDemo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    private String name;
    private String username;
    private String password;
    private int age;
    private String userRoles;

}
