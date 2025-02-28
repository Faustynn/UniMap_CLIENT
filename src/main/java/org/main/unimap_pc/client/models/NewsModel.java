package org.main.unimap_pc.client.models;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NewsModel {
    private int id;
    private String title;
    private String content;
    private String date_of_creation;

    // Constructors, getters, setters...

    public NewsModel() {
    }

    @Override
    public String toString() {
        return "ArticleModel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date_of_creation + '\'' +
                '}';
    }
}
