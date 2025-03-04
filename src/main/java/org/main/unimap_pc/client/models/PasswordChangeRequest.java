package org.main.unimap_pc.client.models;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String email;
    private String newPassword;
}