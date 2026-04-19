module com.example.planetsimdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires firebase.admin;
    requires google.cloud.firestore;
    requires com.google.api.apicommon;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires gax;
    requires gax.grpc;
    requires com.google.protobuf;
    requires com.google.common;
    requires google.cloud.core;

    exports com.example.planetsimdemo;
    opens com.example.planetsimdemo to javafx.fxml;
}