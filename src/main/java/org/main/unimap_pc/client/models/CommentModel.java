package org.main.unimap_pc.client.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentModel {
    private String avatar;
    private String username;
    private String description;
    private String rating;
    private int levelAccess;

    @Override
    public String toString() {
        return "CommentModel{" +
                "avatar.jpg='" + avatar + '\'' +
                ", username='" + username + '\'' +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                ", levelAccess=" + levelAccess +
                '}';
    }

}
