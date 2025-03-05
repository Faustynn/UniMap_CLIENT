package org.main.unimap_pc.client.services;

import java.util.regex.Pattern;

public class SecurityService {
    public boolean checkPassword(String password) {
        // The demands are:
        // Minimum one letter
        // Minimum one digit
        // Minimum 10 symbols in total
        return Pattern.matches(
                "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d#@!$%^&*()_+]{10,}$",
                               password);
    }

    // Here belong: username, name, surname, name of the subject, name of the
    // subject and teacher

    public boolean checkNames(String name) {
        // The demands are:
        // Letters, both upper/lowercase
        // The length varies from 2 symbols to 32 to avoid overloading and
        // suspiciously short patterns (like one letter or number)

        return Pattern.matches("^[a-zA-Z0-9]{2,32}$", name);
    }

    // Finally, email check:
    public boolean checkEmail(String email) {
        // The demands are:
        // Letters and some traditional symbols are allowed
        // The @ obviosly should be and before the domain name
        // The domain afterpart should be at least 2 symbols long (there is no
        // .o or .l domain, but .sg, .jp, .de or .ge do exist)

        return Pattern.matches(
                "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                        email);
    }
}
