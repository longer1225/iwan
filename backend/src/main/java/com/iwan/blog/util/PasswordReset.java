package com.iwan.blog.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码重置工具 - 一次性使用
 * 运行: java -cp target/classes;... com.iwan.blog.util.PasswordReset
 */
public class PasswordReset {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plain = "password";
        String hash = encoder.encode(plain);
        System.out.println("Plain: " + plain);
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + encoder.matches(plain, hash));
    }
}
