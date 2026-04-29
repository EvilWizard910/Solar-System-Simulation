package com.example.planetsimdemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;




import com.google.firebase.auth.*;
import com.google.cloud.firestore.*;
import com.google.api.core.ApiFuture;

import java.io.FileInputStream;
import java.util.Locale;

public class Main extends Application {

    public static Firestore fstore;
    public static FirebaseAuth fauth;
    private final FirestoreContext contxtFirebase = new FirestoreContext();

    private enum SidebarScreen{
        CONTROLS,SYSTEMS
    }

    private record SystemOption(String id, String label){
        @Override public String toString(){
            return label;
        }
    }

    private static void setVisibleManaged(Node node), boolean visible{
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String formatSimulationSpeed(double secondsPerSecond) {
        if (secondsPerSecond < 60) {
            return String.format(Locale.US, "%.1fx real time", secondsPerSecond);
        }
        if (secondsPerSecond < 3600) {
            return String.format(Locale.US, "%.1f min/sec", secondsPerSecond / 60.0);
        }
        if (secondsPerSecond < 86400) {
            return String.format(Locale.US, "%.1f hr/sec", secondsPerSecond / 3600.0);
        }
        return String.format(Locale.US, "%.2f days/sec", secondsPerSecond / 86400.0);
    }

    private static void disableKeyboardFocus(Control... controls) {
        for (Control control : controls) {
            control.setFocusTraversable(false);
        }
    }

    private static double autoDistanceForRadius(double sceneRadius) {
        return Math.max(0.02, Math.min(20.0, sceneRadius * 20.0 + 0.05));
    }

    private static String formatNumber(double value) {
        return Double.toString(value);
    }
    private static double parseOrDefault(TextField field, double defaultValue) {
        String text = field.getText();
        if (text == null) {
            return defaultValue;
        }
        text = text.trim();
        return text.isEmpty() ? defaultValue : Double.parseDouble(text);
    }

    private static boolean hasOrbitInput(TextField semiMajorAxis,
                                         TextField eccentricityField,
                                         TextField inclinationField,
                                         TextField ascendingNodeField,
                                         TextField argumentOfPeriapsisField,
                                         TextField trueAnomaly){
        return !(semiMajorAxis.getText().trim().isEmpty()
        && eccentricityField.getText().trim().isEmpty()
        && inclinationField.getText().trim().isEmpty()
        && ascendingNodeField.getText().trim().isEmpty()
        && trueAnomaly.getText().trim().isEmpty()
        && argumentOfPeriapsisField.getText().trim().isEmpty());}

    private static void updateTypeState(
            ComboBox<String> typeBox,
            ComboBox<String> parentBox,
            TextField massField,
            TextField semiMajorAxisField,
            TextField eccentricityField,
            TextField inclinationField,
            TextField ascendingNodeField,
            TextField argumentOfPeriapsisField,
            TextField trueAnomalyField
    ) {
        String type = typeBox.getValue();
        boolean isMoon = "Moon".equals(type);
        boolean isStar = "Star".equals(type);

        parentBox.setDisable(!isMoon);
        if (!isMoon) {
            parentBox.setValue(null);
        }
        massField.setPromptText(isStar ? "Mass (solar masses)" : "Mass (kg)");

        semiMajorAxisField.setDisable(false);
        eccentricityField.setDisable(false);
        inclinationField.setDisable(false);
        ascendingNodeField.setDisable(false);
        argumentOfPeriapsisField.setDisable(false);
        trueAnomalyField.setDisable(false);

    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Operation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private SolarSystem createFirbaseSolarSystem() {
        try {
            InitialConditionsRepository repository = new InitialConditionsRepository(fstore);
            return new SolarSystem(repository.loadInitialConditions());
        }catch (Exception ex) {
            ex.printStackTrace();
            return new SolarSystem(SolarSystem.defaultInitialConditions());
        }
    }
    @Override
    public void start(Stage stage) {

        fstore = contxtFirebase.firebase();
        fauth = FirebaseAuth.getInstance();

        final SolarSystem[] solarSystemRef =  {
                new SolarSystem(SolarSystem.defaultInitialConditions())
        };
        Group root3D = solarSystemRef[0].getRoot();

        SubScene subScene = new SubScene(
                root3D,
                800, 600,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.BLACK);
        StackPane viewport = new StackPane(subScene);
        viewport.setFocusTraversable(true);
        subScene.widthProperty().bind(viewport.widthProperty());
        subScene.heightProperty().bind(viewport.heightProperty());

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.0001);
        camera.setFarClip(100_000);

        Rotate cameraYawRotate =
                new Rotate(0, Rotate.Y_AXIS);
        Rotate cameraPitchRotate =
                new Rotate(0, Rotate.X_AXIS);
        camera.getTransforms().addAll(cameraYawRotate, cameraPitchRotate);

        subScene.setCamera(camera);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(0);
        root3D.getChildren().add(light);

        AmbientLight ambient = new AmbientLight(Color.color(0.2, 0.2, 0.2));
        root3D.getChildren().add(ambient);

        Rotate tilt =
                new Rotate(90, Rotate.X_AXIS);
        Rotate spin =
                new Rotate(0, Rotate.Y_AXIS);
        Rotate pitch =
                new Rotate(0, Rotate.X_AXIS);
        root3D.getTransforms().addAll(tilt, spin, pitch);

        long[] lastTime = {0};
        final double minSimulationSpeed = 1.0;
        final double maxSimulationSpeed = 604800.0;
        final double[] simulationSpeed = {1.0};

        Body[] currentFocus = {solarSystemRef[0].getBody("Sun")};
        double[] orbitYaw = {35.0};
        double[] orbitPitch = {25.0};
        double[] orbitDistance = {7.0};

        //sidebar to switch between default system and firestore systems
        SidebarScreen[] currentSidebarScreen = {SidebarScreen.CONTROLS};

        boolean[] signedIn = {false};
        String[] signedInEmail = {null};

        ObservableList<SystemOption> availibleSystems=FXCollections.observableArrayList(
                new SystemOption("__default__","Default Solar System")
        );

        solarSystemRef[0].setViewScale(0.0);

        BorderPane root = new BorderPane();
        root.setCenter(viewport);

        VBox controlsBox = new VBox(10);
        controlsBox.setStyle("-fx-padding: 10;");
        controlsBox.setPrefWidth(360);
        controlsBox.setMinWidth(320);

        VBox systemsBox = new VBox(12);
        systemsBox.setPadding(new Insets(10));
        systemsBox.setPrefWidth(360);
        systemsBox.setMinWidth(320);



        Button startStopButton = new Button("Start Simulation");

        Label scaleLabel = new Label("Body Scale");
        Slider scaleSlider = new Slider(0, 1, 0.0);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setMajorTickUnit(0.5);
        scaleSlider.setBlockIncrement(0.1);
        scaleSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                solarSystemRef[0].setViewScale(newValue.doubleValue()));

        Label dtLabel = new Label("Simulation Speed");
        Slider dtSlider = new Slider(0, 1, 0);
        dtSlider.setShowTickLabels(true);
        dtSlider.setShowTickMarks(true);
        dtSlider.setMajorTickUnit(0.25);
        dtSlider.setBlockIncrement(0.05);
        dtSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double t = newValue.doubleValue();
            double speed = minSimulationSpeed * Math.pow(maxSimulationSpeed / minSimulationSpeed, t);
            simulationSpeed[0] = speed;
            dtLabel.setText("Simulation Speed: " + formatSimulationSpeed(speed));
        });

        Label focusLabel = new Label("Focus Body");
        ComboBox<String> focusBox = new ComboBox<>();
        focusBox.getItems().setAll(solarSystemRef[0].getBodyNames());
        focusBox.setValue("Sun");

        Label yawLabel = new Label("Camera Yaw");
        Slider yawSlider = new Slider(-180, 180, orbitYaw[0]);
        yawSlider.setShowTickLabels(true);
        yawSlider.setShowTickMarks(true);
        yawSlider.setMajorTickUnit(90);
        yawSlider.setBlockIncrement(5);
        yawSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitYaw[0] = newValue.doubleValue());

        Label pitchLabel = new Label("Camera Pitch");
        Slider pitchSlider = new Slider(-80, 80, orbitPitch[0]);
        pitchSlider.setShowTickLabels(true);
        pitchSlider.setShowTickMarks(true);
        pitchSlider.setMajorTickUnit(20);
        pitchSlider.setBlockIncrement(2);
        pitchSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitPitch[0] = newValue.doubleValue());

        Label distanceLabel = new Label("Camera Distance");
        Slider distanceSlider = new Slider(0.1, 500.0, orbitDistance[0]);
        distanceSlider.setShowTickLabels(true);
        distanceSlider.setShowTickMarks(true);
        distanceSlider.setMajorTickUnit(5);
        distanceSlider.setBlockIncrement(0.1);
        distanceSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitDistance[0] = newValue.doubleValue());

        ComboBox<String> bodyBox = new ComboBox<>();
        bodyBox.getItems().setAll(solarSystemRef[0].getBodyNames());

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().setAll("Star", "Planet", "Moon");
        typeBox.setValue("Planet");

        ComboBox<String> parentBox = new ComboBox<>();
        parentBox.getItems().setAll(solarSystemRef[0].getBodyNames());
        parentBox.setDisable(true);

        Button refreshBodiesButton = new Button("Refresh Bodies");
        Runnable refreshLists = () -> {
            String selectedBody = bodyBox.getValue();
            String selectedFocus = focusBox.getValue();
            String selectedParent = parentBox.getValue();

            bodyBox.getItems().setAll(solarSystemRef[0].getBodyNames());
            focusBox.getItems().setAll(solarSystemRef[0].getBodyNames());
            parentBox.getItems().setAll(solarSystemRef[0].getBodyNames());

            if (selectedBody != null && solarSystemRef[0].getBody(selectedBody) != null) {
                bodyBox.setValue(selectedBody);
            }
            if (selectedFocus != null && solarSystemRef[0].getBody(selectedFocus) != null) {
                focusBox.setValue(selectedFocus);
            } else {
                focusBox.setValue("Sun");
            }
            if (selectedParent != null && solarSystemRef[0].getBodyNames().contains(selectedParent)) {
                parentBox.setValue(selectedParent);
            }
        };
        refreshBodiesButton.setOnAction(e -> refreshLists.run());

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField massField = new TextField();
        massField.setPromptText("Mass");

        TextField radiusField = new TextField();
        radiusField.setPromptText("Radius (km)");

        TextField semiMajorAxisField = new TextField();
        semiMajorAxisField.setPromptText("Semi-major axis (AU)");

        TextField eccentricityField = new TextField();
        eccentricityField.setPromptText("Eccentricity");

        TextField inclinationField = new TextField();
        inclinationField.setPromptText("Inclination (deg)");

        TextField ascendingNodeField = new TextField();
        ascendingNodeField.setPromptText("Ascending node (deg)");

        TextField argumentOfPeriapsisField = new TextField();
        argumentOfPeriapsisField.setPromptText("Argument of periapsis (deg)");

        TextField trueAnomalyField = new TextField();
        trueAnomalyField.setPromptText("True anomaly (deg)");


        ColorPicker colorPicker = new ColorPicker(Color.WHITE);

        Runnable clearBodySelection = () -> {
            bodyBox.setValue(null);
            nameField.clear();
            massField.clear();
            radiusField.clear();
            semiMajorAxisField.clear();
            eccentricityField.clear();
            inclinationField.clear();
            ascendingNodeField.clear();
            argumentOfPeriapsisField.clear();
            trueAnomalyField.clear();
            colorPicker.setValue(Color.WHITE);
            typeBox.setValue("Planet");
            parentBox.setValue(null);
            updateTypeState(typeBox, parentBox, massField, semiMajorAxisField, eccentricityField, inclinationField,
                    ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField);
        };

        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setOnAction(e -> clearBodySelection.run());

        typeBox.valueProperty().addListener((obs, oldValue, newValue) ->
                updateTypeState(typeBox, parentBox, massField, semiMajorAxisField, eccentricityField, inclinationField,
                        ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField));

        bodyBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            Body selected = solarSystemRef[0].getBody(newValue);
            if (selected == null) {
                return;
            }
            SolarSystem.OrbitElements orbit = solarSystemRef[0].getOrbitElements(newValue);
            nameField.setText(selected.getName());
            massField.setText(formatNumber("Star".equals(solarSystemRef[0].getBodyType(newValue))
                    ? selected.getMass() / Conversions.massOfSun
                    : selected.getMass()
            ));
            radiusField.setText(formatNumber(solarSystemRef[0].getBodyRadiusKm(newValue)));
            colorPicker.setValue(solarSystemRef[0].getBodyColor(newValue));
            typeBox.setValue(solarSystemRef[0].getBodyType(newValue));
            parentBox.getItems().setAll(solarSystemRef[0].getBodyNames());
            parentBox.setValue(solarSystemRef[0].getOrbitParent(newValue));

            if (orbit != null) {
                semiMajorAxisField.setText(formatNumber(orbit.semiMajorAxisAu()));
                eccentricityField.setText(formatNumber(orbit.eccentricity()));
                inclinationField.setText(formatNumber(orbit.inclinationDeg()));
                ascendingNodeField.setText(formatNumber(orbit.ascendingNodeDeg()));
                argumentOfPeriapsisField.setText(formatNumber(orbit.argumentOfPeriapsisDeg()));
                trueAnomalyField.setText(formatNumber(orbit.trueAnomalyDeg()));
            } else {
                semiMajorAxisField.setText("0.0");
                eccentricityField.setText("0.0");
                inclinationField.setText("0.0");
                ascendingNodeField.setText("0.0");
                argumentOfPeriapsisField.setText("0.0");
                trueAnomalyField.setText("0.0");
            }
            updateTypeState(
                    typeBox, parentBox,massField,
                    semiMajorAxisField, eccentricityField, inclinationField,
                    ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField
            );
        });

        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button removeButton = new Button("Remove");

        // Reset Simulation Button
        Button resetSimulationButton = new Button("Reset Simulation");

        resetSimulationButton.setOnAction(e -> {
            SolarSystem newSystem = new SolarSystem(SolarSystem.defaultInitialConditions());
            solarSystemRef[0]=newSystem;

            subScene.setRoot(newSystem.getRoot());

            currentFocus[0] = newSystem.getBody("Sun");

            focusBox.getItems().setAll(newSystem.getBodyNames());
            focusBox.setValue("Sun");

            focusBox.getItems().setAll(newSystem.getBodyNames());
            focusBox.setValue(null);

            parentBox.getItems().setAll(newSystem.getBodyNames());
            parentBox.setValue(null);

            clearBodySelection.run();

            orbitYaw[0] = 35.0;
            orbitPitch[0] = 25.0;

            Body sun = newSystem.getBody("Sun");
            orbitDistance[0] = sun == null ? 7.0 : autoDistanceForRadius(sun.getView().getRadius());

            yawSlider.setValue(orbitYaw[0]);
            pitchSlider.setValue(orbitPitch[0]);
            distanceSlider.setValue(orbitDistance[0]);

            scaleSlider.setValue(0.0);
            dtSlider.setValue(0.0);

        });

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Name is required.");
                    return;
                }

                String type = typeBox.getValue();
                String parent = "Moon".equals(type) ? parentBox.getValue() : null;
                if ("Moon".equals(type) && (parent == null || parent.isBlank())) {
                    showError("Moons must have a parent planet.");
                    return;
                }
                boolean givenOrbit = hasOrbitInput(semiMajorAxisField,eccentricityField,inclinationField,ascendingNodeField,argumentOfPeriapsisField,trueAnomalyField);

                double semiMajorAxisAu= 0.0;
                double eccentricity = 0.0;
                double inclinationDeg = 0.0;
                double ascendingNodeDeg = 0.0;
                double argumentOfPeriapsisDeg = 0.0;
                double trueAnomalyDeg = 0.0;

                double enteredMass = Double.parseDouble(massField.getText().trim());
                double mass = "Star".equals(type)
                        ? enteredMass * Conversions.massOfSun
                        : enteredMass;
                double radiusKm = Double.parseDouble(radiusField.getText().trim());
                if(!"Star".equals(type) || givenOrbit){
                    semiMajorAxisAu = parseOrDefault(semiMajorAxisField, 0.0);
                    eccentricity = parseOrDefault(eccentricityField, 0.0);
                    inclinationDeg = parseOrDefault(inclinationField, 0.0);
                    ascendingNodeDeg = parseOrDefault(ascendingNodeField, 0.0);
                    argumentOfPeriapsisDeg = parseOrDefault(argumentOfPeriapsisField, 0.0);
                    trueAnomalyDeg = parseOrDefault(trueAnomalyField, 0.0);
                }


                boolean added=solarSystemRef[0].addNewBody(
                        name, type, parent, mass, radiusKm,
                        semiMajorAxisAu, eccentricity, inclinationDeg,
                        ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg,
                        colorPicker.getValue()
                );

                if (!added) {
                    showError("Could not add body. Check for duplicate names or invalid moon parent.");
                    return;
                }

                refreshLists.run();
                bodyBox.setValue(name);
                focusBox.setValue(name);
            } catch (Exception ex) {
                showError("Please enter valid numeric values.");
            }
        });

        editButton.setOnAction(e -> {
            String selected = bodyBox.getValue();
            if (selected == null) {
                showError("Select a body to edit.");
                return;
            }

            try {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    showError("Name is required.");
                    return;
                }

                String type = typeBox.getValue();
                String parent = "Moon".equals(type) ? parentBox.getValue() : null;
                if ("Moon".equals(type) && (parent == null || parent.isBlank())) {
                    showError("Moons must have a parent planet.");
                    return;
                }

                boolean givenOrbit = hasOrbitInput(semiMajorAxisField,eccentricityField,inclinationField,ascendingNodeField,argumentOfPeriapsisField,trueAnomalyField);

                double semiMajorAxisAu= 0.0;
                double eccentricity = 0.0;
                double inclinationDeg = 0.0;
                double ascendingNodeDeg = 0.0;
                double argumentOfPeriapsisDeg = 0.0;
                double trueAnomalyDeg = 0.0;

                double enteredMass = Double.parseDouble(massField.getText().trim());
                double mass = "Star".equals(type)
                        ? enteredMass * Conversions.massOfSun
                        : enteredMass;
                double radiusKm = Double.parseDouble(radiusField.getText().trim());
                if(!"Star".equals(type) || givenOrbit){
                    semiMajorAxisAu = parseOrDefault(semiMajorAxisField, 0.0);
                    eccentricity = parseOrDefault(eccentricityField, 0.0);
                    inclinationDeg = parseOrDefault(inclinationField, 0.0);
                    ascendingNodeDeg = parseOrDefault(ascendingNodeField, 0.0);
                    argumentOfPeriapsisDeg = parseOrDefault(argumentOfPeriapsisField, 0.0);
                    trueAnomalyDeg = parseOrDefault(trueAnomalyField, 0.0);}

                Color color = colorPicker.getValue();

                boolean wasFocused = currentFocus[0] != null && selected.equals(currentFocus[0].getName());

                boolean updated = solarSystemRef[0].updateBody(selected, newName, type, parent, mass, radiusKm, semiMajorAxisAu, eccentricity, inclinationDeg,
                        ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg, color);
                if (!updated) {
                    showError("Could not edit body. Names must be unique, moons need a planet parent, and parents with moons cannot stop being planets.");
                    return;
                }

                refreshLists.run();
                bodyBox.setValue(newName);

                if (wasFocused) {
                    currentFocus[0] = solarSystemRef[0].getBody(newName);
                    focusBox.setValue(newName);
                }
            } catch (Exception ex) {
                showError("Please enter valid numeric values.");
            }
        });

        removeButton.setOnAction(e -> {
            String selected = bodyBox.getValue();
            if (selected == null) {
                showError("Select a body to remove.");
                return;
            }

            boolean wasFocused = currentFocus[0] != null && selected.equals(currentFocus[0].getName());

            boolean removed = solarSystemRef[0].removeBody(selected);
            if (!removed) {
                showError("Remove moons orbiting this body first.");
                return;
            }

            refreshLists.run();

            if (wasFocused) {
                currentFocus[0] = solarSystemRef[0].getBody("Sun");
                focusBox.setValue("Sun");
            }

            clearBodySelection.run();
        });

        Runnable resetCamera = () -> {
            currentFocus[0] = solarSystemRef[0].getBody("Sun");
            focusBox.setValue("Sun");

            orbitYaw[0] = 35.0;
            orbitPitch[0] = 25.0;

            Body sun = solarSystemRef[0].getBody("Sun");
            orbitDistance[0] = sun == null ? 7.0 : autoDistanceForRadius(sun.getView().getRadius());

            yawSlider.setValue(orbitYaw[0]);
            pitchSlider.setValue(orbitPitch[0]);
            distanceSlider.setValue(orbitDistance[0]);
        };

        Button resetCameraButton = new Button("Reset Camera");
        resetCameraButton.setOnAction(e -> resetCamera.run());

        focusBox.setOnAction(e -> {
            String selected = focusBox.getValue();
            if (selected == null) return;

            Body focused = solarSystemRef[0].getBody(selected);
            if (focused == null) return;

            currentFocus[0] = focused;
            orbitDistance[0] = autoDistanceForRadius(focused.getView().getRadius());
            distanceSlider.setValue(orbitDistance[0]);
        });

        final boolean[] isRunning = {true};

        final AnimationTimer timer = new AnimationTimer() {
            private static final double MAX_PHYSICS_STEP = 120.0; // 5 minutes
            private static final double MAX_FRAME_DT = 0.25;      // avoid giant jumps after lag

            @Override
            public void handle(long now) {
                if (lastTime[0] == 0) {
                    lastTime[0] = now;
                    return;
                }

                double dt = (now - lastTime[0]) / 1_000_000_000.0;
                lastTime[0] = now;
                dt = Math.min(dt, MAX_FRAME_DT);

                double simulatedDt = dt * simulationSpeed[0];

                while (simulatedDt > 0) {
                    double step = Math.min(simulatedDt, MAX_PHYSICS_STEP);
                    solarSystemRef[0].updatePhysics(step);
                    simulatedDt -= step;
                }

                if (currentFocus[0] != null) {
                    double targetX = Conversions.metersToScene(currentFocus[0].getX());
                    double targetY = -Conversions.metersToScene(currentFocus[0].getZ());
                    double targetZ = Conversions.metersToScene(currentFocus[0].getY());

                    double yawRad = Math.toRadians(orbitYaw[0]);
                    double pitchRad = Math.toRadians(orbitPitch[0]);

                    double horizontal = orbitDistance[0] * Math.cos(pitchRad);

                    double camX = targetX + Math.sin(yawRad) * horizontal;
                    double camY = targetY - Math.sin(pitchRad) * orbitDistance[0];
                    double camZ = targetZ - Math.cos(yawRad) * horizontal;

                    camera.setTranslateX(camX);
                    camera.setTranslateY(camY);
                    camera.setTranslateZ(camZ);

                    double dx = targetX - camX;
                    double dy = targetY - camY;
                    double dz = targetZ - camZ;

                    double lookYaw = Math.toDegrees(Math.atan2(dx, dz));
                    double lookPitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

                    cameraYawRotate.setAngle(lookYaw);
                    cameraPitchRotate.setAngle(lookPitch);
                }
            }
        };

        startStopButton.setOnAction(e -> {
            if (isRunning[0]) {
                timer.stop();
                startStopButton.setText("▶");
                isRunning[0] = false;
            } else {
                lastTime[0] = 0;
                timer.start();
                startStopButton.setText("⏹");
                isRunning[0] = true;
            }
        });

        disableKeyboardFocus(
                resetSimulationButton,
                startStopButton,
                scaleSlider,
                dtSlider,
                focusBox,
                yawSlider,
                pitchSlider,
                distanceSlider,
                bodyBox,
                typeBox,
                parentBox,
                colorPicker,
                refreshBodiesButton,
                clearSelectionButton,
                addButton,
                editButton,
                removeButton,
                resetCameraButton
        );

        resetCamera.run();
        timer.start();

        controlsBox.getChildren().addAll(
                startStopButton,
                scaleLabel,
                scaleSlider,
                dtLabel,
                dtSlider,
                new Separator(),
                focusLabel,
                focusBox,
                yawLabel,
                yawSlider,
                pitchLabel,
                pitchSlider,
                distanceLabel,
                distanceSlider,
                new Separator(),
                new Label("Bodies"),
                bodyBox,
                new HBox(5, refreshBodiesButton, clearSelectionButton),
                nameField,
                massField,
                radiusField,
                semiMajorAxisField,
                eccentricityField,
                inclinationField,
                ascendingNodeField,
                argumentOfPeriapsisField,
                trueAnomalyField,
                new Label("Type"),
                typeBox,
                new Label("Parent Planet"),
                parentBox,
                new Label("Color"),
                colorPicker,
                new HBox(5, addButton, editButton, removeButton),
                resetCameraButton,
                resetSimulationButton
        );

        // Scroll feature
        ScrollPane controlsScrollPane = new ScrollPane(controlsBox);
        controlsScrollPane.setFitToWidth(true);
        controlsScrollPane.setPannable(true);
        controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlsScrollPane.setPrefWidth(380);

        root.setRight(controlsScrollPane);

        Scene scene = new Scene(root, 1400, 900, true);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (scene.getFocusOwner() instanceof TextInputControl) {
                return;
            }

            boolean handled = true;
            KeyCode code = event.getCode();

            switch (code) {
                case UP -> distanceSlider.setValue(Math.max(distanceSlider.getMin(), distanceSlider.getValue() - 0.1));
                case DOWN -> distanceSlider.setValue(Math.min(distanceSlider.getMax(), distanceSlider.getValue() + 0.1));

                case LEFT -> yawSlider.setValue(yawSlider.getValue() - 5);
                case RIGHT -> yawSlider.setValue(yawSlider.getValue() + 5);

                case W -> pitchSlider.setValue(Math.min(pitchSlider.getMax(), pitchSlider.getValue() + 3));
                case S -> pitchSlider.setValue(Math.max(pitchSlider.getMin(), pitchSlider.getValue() - 3));

                case A -> yawSlider.setValue(yawSlider.getValue() - 1);
                case D -> yawSlider.setValue(yawSlider.getValue() + 1);

                case I -> distanceSlider.setValue(Math.max(distanceSlider.getMin(), distanceSlider.getValue() - 0.01));
                case K -> distanceSlider.setValue(Math.min(distanceSlider.getMax(), distanceSlider.getValue() + 0.01));

                case J -> pitchSlider.setValue(Math.min(pitchSlider.getMax(), pitchSlider.getValue() + 1));
                case L -> pitchSlider.setValue(Math.max(pitchSlider.getMin(), pitchSlider.getValue() - 1));

                default -> handled = false;
            }

            if (handled) {
                event.consume();
            }
        });

        final double[] lastMouseX = {0};
        final double[] lastMouseY = {0};

        viewport.setOnMouseClicked(event -> {
            viewport.requestFocus();
            event.consume();
        });

        viewport.setOnMousePressed(event -> {
            viewport.requestFocus();

            lastMouseX[0] = event.getSceneX();
            lastMouseY[0] = event.getSceneY();

            event.consume();
        });

        viewport.setOnMouseDragged(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();

            double deltaX = mouseX - lastMouseX[0];
            double deltaY = mouseY - lastMouseY[0];

            if (event.isPrimaryButtonDown()) {
                double rotateSensitivity = 0.25;

                double newYaw = yawSlider.getValue() + deltaX * rotateSensitivity;
                double newPitch = pitchSlider.getValue() - deltaY * rotateSensitivity;

                yawSlider.setValue(
                        Math.max(yawSlider.getMin(), Math.min(yawSlider.getMax(), newYaw))
                );

                pitchSlider.setValue(
                        Math.max(pitchSlider.getMin(), Math.min(pitchSlider.getMax(), newPitch))
                );
            }

            if (event.isSecondaryButtonDown()) {
                double zoomSensitivity = 0.01;
                double zoomFactor = Math.pow(1.0 + zoomSensitivity, deltaY);

                double newDistance = distanceSlider.getValue() * zoomFactor;

                distanceSlider.setValue(
                        Math.max(distanceSlider.getMin(), Math.min(distanceSlider.getMax(), newDistance))
                );
            }

            lastMouseX[0] = mouseX;
            lastMouseY[0] = mouseY;

            event.consume();
        });

        viewport.setOnContextMenuRequested(event -> event.consume());

        stage.setTitle("Planetary Simulation");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        viewport.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}