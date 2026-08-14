package com.example.MultiUserSecurityDemo.common;

import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType1Details;
import com.example.MultiUserSecurityDemo.adapter.security.user_details.UserType2Details;
import com.example.MultiUserSecurityDemo.domain.model.UserRoles1;
import com.example.MultiUserSecurityDemo.domain.model.UserRoles2;
import com.example.MultiUserSecurityDemo.domain.model.UserType1;
import com.example.MultiUserSecurityDemo.domain.model.UserType2;

public class TestUsers {

    private TestUsers() {}

    // ==========================================================
    // DOMAIN USERS
    // ==========================================================

    public static UserType1 adminDomain() {

        UserType1 user = new UserType1();

        user.setId(1L);
        user.setFname("Admin");
        user.setLname("User");
        user.setEmail("admin@test.com");
        user.setPassword("password");
        user.setPhoneNumber("9876543210");
        user.setRoles1(UserRoles1.ADMIN);
        user.setApproved(true);
        user.setEmailVerified(true);

        return user;
    }

    public static UserType1 normalType1Domain() {

        UserType1 user = new UserType1();

        user.setId(2L);
        user.setFname("John");
        user.setLname("Doe");
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setPhoneNumber("9999999999");
        user.setRoles1(UserRoles1.ADMIN_TYPE1);
        user.setApproved(true);
        user.setEmailVerified(true);

        return user;
    }

    public static UserType2 adminType2Domain() {

        UserType2 user = new UserType2();

        user.setId(3L);
        user.setFname("Admin");
        user.setLname("Type2");
        user.setEmail("admin2@test.com");
        user.setPassword("password");
        user.setPhoneNumber("8888888888");
        user.setRole(UserRoles2.USER);
        user.setApproved(true);
        user.setEmailVerified(true);

        return user;
    }

    public static UserType2 normalType2Domain() {

        UserType2 user = new UserType2();

        user.setId(4L);
        user.setFname("Jane");
        user.setLname("Smith");
        user.setEmail("user2@test.com");
        user.setPassword("password");
        user.setPhoneNumber("7777777777");
        user.setRole(UserRoles2.USER_TYPE2);
        user.setApproved(true);
        user.setEmailVerified(true);

        return user;
    }

    // ==========================================================
    // USER DETAILS
    // ==========================================================

    public static UserType1Details admin() {
        return new UserType1Details(adminDomain());
    }

    public static UserType1Details user() {
        return new UserType1Details(normalType1Domain());
    }

    public static UserType2Details adminType2() {
        return new UserType2Details(adminType2Domain());
    }

    public static UserType2Details userType2() {
        return new UserType2Details(normalType2Domain());
    }

}
