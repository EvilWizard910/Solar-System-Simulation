module com.example.planetsimdemo {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports com.example.planetsimdemo;
    opens com.example.planetsimdemo to javafx.fxml;
}