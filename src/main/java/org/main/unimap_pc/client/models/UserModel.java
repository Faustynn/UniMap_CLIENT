package org.main.unimap_pc.client.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserModel {
    private String id;
    private String username;
    private String email;
    private String login;
    private boolean isAdmin;
    private String avatar;

}