package com.example.MultiUserSecurityDemo.controller;

import com.example.MultiUserSecurityDemo.enums.UserRoles1;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/type1")
@CrossOrigin("*")
public class UserType1Controller {

    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, Object>> adminDashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN Profile");
        response.put("userType", "TYPE1");
        response.put("role", UserRoles1.ADMIN);
        response.put("email", "");
        response.put("fullName", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type1/dashboard")
    public ResponseEntity<Map<String, Object>> userType1Dashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN TYPE 1 Profile");
        response.put("userType", "TYPE1");
        response.put("role", UserRoles1.ADMIN_TYPE1);
        response.put("email", "");
        response.put("fullName", "");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type2/dashboard")
    public ResponseEntity<Map<String, Object>> userType2Dashboard() {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN TYPE 2 Profile");
        response.put("userType", "TYPE1");
        response.put("role", UserRoles1.ADMIN_TYPE2);
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
