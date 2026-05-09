module com.example.planetsimdemo {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;
    requires java.net.http;
    requires java.sql;

    exports com.example.planetsimdemo;
    opens com.example.planetsimdemo to javafx.fxml;
}
