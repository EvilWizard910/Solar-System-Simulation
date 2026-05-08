package com.example.planetsimdemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SolarSystem solarSystem = new SolarSystem(SolarSystemState.defaultInitialConditions());

        SimulationScreen simulationScreen = new SimulationScreen(solarSystem);
        Parent simulationRoot = simulationScreen.build();

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Design2.fxml"));
        VBox UI = loader.load();

        Design2Controller design2Controller = loader.getController();
        design2Controller.setSolarSystem(solarSystem);
        design2Controller.setSimulationScreen(simulationScreen);

        StackPane root = new StackPane(simulationRoot,UI);
        StackPane.setAlignment(UI, Pos.CENTER_RIGHT);


        Scene scene = new Scene(root, 1400, 900);

        stage.setTitle("Planetary Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
