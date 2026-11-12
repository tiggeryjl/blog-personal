package com.blog.utils;

import java.util.Set;

public class MailUtil {
    private static final Set<String> BAD_EMAIL_DOMAIN = Set.of(
            "example.invalid",
            "example.com",
            "example.org",
            "test.com",
            "demo.com",
            "test.invalid"
    );

    public static boolean isBadTestEmail(String email) {
        if (!email.contains("@")) return true;
        String domain = email.split("@")[1];
        return BAD_EMAIL_DOMAIN.contains(domain);
    }
}
