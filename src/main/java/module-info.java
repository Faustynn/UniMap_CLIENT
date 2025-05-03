module org.main.unimap_pc {
    requires static lombok;
    requires MaterialFX;
    requires java.net.http;
    requires fontawesomefx;
    requires com.fasterxml.jackson.databind;
    requires org.json;
    requires java.prefs;
    requires jdk.httpserver;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires javax.websocket.api;
    requires okhttp3;
    requires okhttp3.sse;
    requires io.github.cdimascio.dotenv.java;


    exports org.main.unimap_pc.client.models;
    exports org.main.unimap_pc.client to javafx.graphics;
    exports org.main.unimap_pc.client.controllers to javafx.fxml;
    opens org.main.unimap_pc.client to javafx.fxml, javafx.graphics;
    opens org.main.unimap_pc.client.controllers to javafx.fxml;
}