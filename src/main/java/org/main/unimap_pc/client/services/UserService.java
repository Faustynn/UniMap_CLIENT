package org.main.unimap_pc.client.services;

import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.models.UserModel;

@Getter
@Setter
public class UserService {
    private static UserService instance;
    private UserModel currentUser;
    private String accessToken;
    private String refreshToken;
    private String defLang;
    private UserService() {}

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    

}