package com.creditapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt Password Generator ===");
        System.out.println();
        
        String parentPassword = "parent123";
        String childPassword = "child123";
        
        String parentHash = encoder.encode(parentPassword);
        String childHash = encoder.encode(childPassword);
        
        System.out.println("Parent password: " + parentPassword);
        System.out.println("Parent BCrypt hash: " + parentHash);
        System.out.println();
        System.out.println("Child password: " + childPassword);
        System.out.println("Child BCrypt hash: " + childHash);
        System.out.println();
        
        // Verify the hashes
        System.out.println("=== Verification ===");
        System.out.println("Parent password matches hash: " + encoder.matches(parentPassword, parentHash));
        System.out.println("Child password matches hash: " + encoder.matches(childPassword, childHash));
        
        System.out.println();
        System.out.println("=== SQL Statements ===");
        System.out.println("-- Parent user:");
        System.out.println("INSERT INTO users (username, password, role, points) VALUES ('parent', '" + parentHash + "', 'PARENT', 0);");
        System.out.println();
        System.out.println("-- Child user:");
        System.out.println("INSERT INTO children (username, password, role, parent_id, points) VALUES ('child', '" + childHash + "', 'CHILD', 1, 0);");
    }
}