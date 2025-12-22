package com.example.MultiUserSecurityDemo.adapter.web.controller;

import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Map<String, Object>> adminDashboard(
            @AuthenticationPrincipal UserType1Details userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN Dashboard");
        response.put("userType", "TYPE1");
        response.put("role", userDetails.getAuthorities());
        response.put("email", userDetails.getUsername());
        response.put("fullName", userDetails.getUser().getFname() + " " + userDetails.getUser().getLname());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type1/dashboard")
    public ResponseEntity<Map<String, Object>> adminType1Dashboard(
            @AuthenticationPrincipal UserType1Details userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN_TYPE1 Dashboard");
        response.put("userType", "TYPE1");
        response.put("role", userDetails.getAuthorities());
        response.put("email", userDetails.getUsername());
        response.put("fullName", userDetails.getUser().getFname() + " " + userDetails.getUser().getLname());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin-type2/dashboard")
    public ResponseEntity<Map<String, Object>> adminType2Dashboard(
            @AuthenticationPrincipal UserType1Details userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to ADMIN_TYPE2 Dashboard");
        response.put("userType", "TYPE1");
        response.put("role", userDetails.getAuthorities());
        response.put("email", userDetails.getUsername());
        response.put("fullName", userDetails.getUser().getFname() + " " + userDetails.getUser().getLname());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-admin/content")
    public ResponseEntity<Map<String, Object>> allAdminContent(
            @AuthenticationPrincipal UserType1Details userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Content accessible to all admin types");
        response.put("userType", "TYPE1");
        response.put("role", userDetails.getAuthorities());
        response.put("email", userDetails.getUsername());
        response.put("data", "Shared admin resources");

        return ResponseEntity.ok(response);
    }
}

