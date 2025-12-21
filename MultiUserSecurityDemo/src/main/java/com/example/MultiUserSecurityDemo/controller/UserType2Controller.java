package com.example.MultiUserSecurityDemo.controller;


import com.example.MultiUserSecurityDemo.enums.UserRoles2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/type2")
@CrossOrigin("*")
public class UserType2Controller {

    @GetMapping("/user/dashboard")
    public ResponseEntity<Map<String, Object>> userDashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to USER Profile");
        response.put("userType", "TYPE1");
        response.put("role", UserRoles2.USER);
        response.put("email", "");
        response.put("fullName", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type1/dashboard")
    public ResponseEntity<Map<String, Object>> userType1Dashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to USER TYPE 1 Profile");
        response.put("userType", "TYPE2");
        response.put("role", UserRoles2.USER_TYPE1);
        response.put("email", "");
        response.put("fullName", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type2/dashboard")
    public ResponseEntity<Map<String, Object>> userType2Dashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to USER TYPE 2 Profile");
        response.put("userType", "TYPE1");
        response.put("role", UserRoles2.USER_TYPE2);
        response.put("email", "");
        response.put("fullName", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-admin/content")
    public ResponseEntity<Map<String, Object>> allUserContent() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Content accessible to all user types");
        response.put("userType", "TYPE1");
        response.put("role", "");
        response.put("email", "");
        response.put("data", "");
        response.put("loggedInName", "");

        return ResponseEntity.ok(response);
    }


}
