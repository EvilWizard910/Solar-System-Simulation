package com.example.planetsimdemo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class Design2Controller {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signOutButton;
    @FXML private TextField saveNameField;
    @FXML private Button saveButton;
    @FXML private ListView<String> savedSystemsList;

    @FXML private ComboBox<String> focusDropdown;
    @FXML private Label massLabel;
    @FXML private Label radiusKmLabel;
    @FXML private Label semiMajorAxisAuLabel;
    @FXML private Label eccentricityLabel;
    @FXML private Label inclinationDegreeLabel;
    @FXML private Label ascendingNodeDegreeLabel;
    @FXML private Label argumentOfPeriapsisDegreeLabel;
    @FXML private Slider timeScaleSlider;
    @FXML private Slider sizeScaleSlider;
    @FXML private Label timeScaleLabel;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeDropdown;
    @FXML private ComboBox<String> parentDropdown;
    @FXML private TextField massField;
    @FXML private TextField radiusKmField;
    @FXML private TextField semiMajorAxisAuField;
    @FXML private TextField eccentricityField;
    @FXML private TextField inclinationDegreeField;
    @FXML private TextField ascendingNodeDegreeField;
    @FXML private TextField argumentOfPeriapsisDegreeField;
    @FXML private  TextField trueAnomalyDegField;
    @FXML private ColorPicker colorPicker;
    @FXML private Label addTextureLabel;
    @FXML private Button addTextureButton;

    @FXML private ListView<String> bodyList;
    @FXML private Slider massSlider;
    @FXML private Slider radiusKmSlider;
    @FXML private Slider semiMajorAxisAuSlider;
    @FXML private Slider eccentricitySlider;
    @FXML private Slider inclinationDegreeSlider;
    @FXML private Slider ascendingNodeDegreeSlider;
    @FXML private Slider argumentOfPeriapsisDegreeSlider;
    @FXML private Label editTextureLabel;
    @FXML private Button editTextureButton;
    @FXML private TextField rotationSpeedDegPerSecondField;
    @FXML private Slider rotationSpeedSlider;

    private SolarSystem solarSystem;
    private AuthViewModel authViewModel;
    private BodyEditorViewModel bodyEditorViewModel;
    private SimulationScreen simulationScreen;

    private double selectedBaseMass = 1.0;
    private double selectedBaseRadiusKm = 1.0;
    private double selectedBaseSemiMajorAxisAu = 1.0;

    private static final double minTimeScale=1.0;
    private static final double maxTimeScale=7*24*3600;
    private static final long MAX_TEXTURE_BYTES = 2 * 1024 * 1024; // 2 MB
    private static final double MAX_TEXTURE_WIDTH = 2048;
    private static final double MAX_TEXTURE_HEIGHT = 2048;

    public void initialize(){
        authViewModel = new AuthViewModel(
                new FirebaseAuthenticationService(),
                new InitialConditionsRepository());

        bindAuthSection();
        bindViewSection();
        bindEditSection();
    }

    private void configureStaticUi(){
        typeDropdown.setItems(bodyEditorViewModel.getAvailableTypes());
        parentDropdown.setItems(bodyEditorViewModel.getAvailableParents());
        bodyList.setItems(bodyEditorViewModel.getAvailableBodies());
        savedSystemsList.setItems(authViewModel.getSavedSystems());

        colorPicker.setValue(Color.WHITE);
        parentDropdown.setDisable(true);
        signOutButton.setDisable(true);
        saveButton.setDisable(true);
    }

    private void bindAuthSection(){
        emailField.textProperty().bindBidirectional(authViewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(authViewModel.passwordProperty());
        saveNameField.textProperty().bindBidirectional(authViewModel.saveNameProperty());
        savedSystemsList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
            authViewModel.selectedSavedSystemProperty().set(newValue)
        );

        authViewModel.currentSessionProperty().addListener((obs, oldValue, newValue) ->{
            boolean signedIn=newValue!=null&&newValue.isAuthenticated();
            signOutButton.setDisable(!signedIn);
            saveButton.setDisable(!signedIn);
        });
    }
    private void bindBodyEditorSection(){
        nameField.textProperty().bindBidirectional(bodyEditorViewModel.nameProperty());
        typeDropdown.valueProperty().bindBidirectional(bodyEditorViewModel.typeProperty());
        parentDropdown.valueProperty().bindBidirectional(bodyEditorViewModel.parentProperty());
        colorPicker.valueProperty().bindBidirectional(bodyEditorViewModel.colorProperty());

        typeDropdown.valueProperty().addListener((obs, oldValue, newValue) ->{
            boolean isMoon="Moon".equals(newValue);
            parentDropdown.setDisable(!isMoon);
            if(!isMoon){
                parentDropdown.setValue(null);
            }
        });
    }

    private void bindViewSection(){
        focusDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->{
                updateFocusedBodyDetails(newValue);
            if(simulationScreen!=null) {
                simulationScreen.setFocusedBody(newValue);
            }
        });

        timeScaleSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            applyTimeScale(newValue.doubleValue());
        });
        applyTimeScale(timeScaleSlider.getValue());

        sizeScaleSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(simulationScreen!=null) {
                simulationScreen.setSizeScale(newValue.doubleValue());
            }
        });
    }

    private void bindEditSection(){
        bodyList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            bodyEditorViewModel.selectedBodyNameProperty().set(newValue);
            bodyEditorViewModel.loadSelectedBody();
            pushEditorValuesToUi();
            captureSelectedBodyBases();
            syncEditSlidersFromForm();
        });
        massSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double mass = selectedBaseMass*multiplierFromSlider(newValue.doubleValue());
            massField.setText(formatDouble(mass));
            applyLiveEdit();
        });
        radiusKmSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
           double radiusKm = selectedBaseRadiusKm*newValue.doubleValue();
            radiusKmField.setText(formatDouble(radiusKm));
            applyLiveEdit();
        });
        semiMajorAxisAuSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double axis = selectedBaseSemiMajorAxisAu*newValue.doubleValue();
            semiMajorAxisAuField.setText(formatDouble(axis));
            applyLiveEdit();
        });
        eccentricitySlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            eccentricityField.setText(formatDouble(newValue.doubleValue()));
            applyLiveEdit();
        });
        inclinationDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            inclinationDegreeField.setText(formatDouble(newValue.doubleValue()));
            applyLiveEdit();
        });
        ascendingNodeDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            ascendingNodeDegreeField.setText(formatDouble(newValue.doubleValue()));
            applyLiveEdit();
        });
        argumentOfPeriapsisDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            argumentOfPeriapsisDegreeField.setText(formatDouble(newValue.doubleValue()));
            applyLiveEdit();
        });
        rotationSpeedSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            rotationSpeedDegPerSecondField.setText(Double.toString(newValue.doubleValue()));
        });
    }

    private void applyLiveEdit(){
        if (bodyEditorViewModel==null){
            return;
        }
        String selected = bodyEditorViewModel.selectedBodyNameProperty().get();
        if (selected==null||selected.isBlank()){
            return;
        }
        pullEditorValuesFromUi();
        if (bodyEditorViewModel.updateBody()&&simulationScreen!=null){
            simulationScreen.buildBodies();
        }
    }

    public void setSolarSystem(SolarSystem solarSystem){
        this.solarSystem=solarSystem;
        this.bodyEditorViewModel = new BodyEditorViewModel(solarSystem);

        configureStaticUi();
        bindBodyEditorSection();
        refreshAllUi();
    }
    @FXML
    private void onSignIn(){
        authViewModel.signIn();
        showAuthErrorIfPresent();
    }
    @FXML
    private void onRegister(){
        authViewModel.signUp();
        showAuthErrorIfPresent();
    }

    @FXML
    private void onSignOut(){
        authViewModel.signOut();
        showAuthErrorIfPresent();
    }

    @FXML
    private void onSaveSystem(){
        authViewModel.saveCurrentSession(solarSystem.getState());
        showAuthErrorIfPresent();
    }

    @FXML
    private void onLoadSelectedSystem(){
        try{
            SolarSystemState loadedState=authViewModel.loadSelectedSystem();
            solarSystem=new SolarSystem(loadedState);
            bodyEditorViewModel.setSolarSystem(solarSystem);
            if(simulationScreen!=null){
                simulationScreen.setSolarSystem(solarSystem);
            }
            refreshAllUi();
        }catch(Exception e){
            showError(e.getMessage());
        }
    }

    @FXML
    private void onAddBody(){
        pullEditorValuesFromUi();
        if(!bodyEditorViewModel.addBody()){
            showError(bodyEditorViewModel.errorMessageProperty().get());
            return;
        }
        refreshAllUi();
        if(simulationScreen!=null){
            simulationScreen.buildBodies();
        }
        clearAddTextureSelection();
    }

    @FXML
    private void onEditSelectedBody(){
        pullEditorValuesFromUi();
        if(!bodyEditorViewModel.updateBody()){
            showError(bodyEditorViewModel.errorMessageProperty().get());
            return;
        }
        refreshAllUi();
        if(simulationScreen!=null){
            simulationScreen.buildBodies();
        }
    }

    @FXML
    private void onDeleteSelectedBody(){
        if(!bodyEditorViewModel.removeSelectedBody()){
            showError(bodyEditorViewModel.errorMessageProperty().get());
            return;
        }
        refreshAllUi();
        if(simulationScreen!=null){
            simulationScreen.buildBodies();
        }
        clearEditTextureSelection();
    }

    @FXML
    private void onAddTexture() {
        try {
            String copiedTexturePath = chooseAndCopyTextureFile();
            if (copiedTexturePath == null) {
                return;
            }

            bodyEditorViewModel.texturePathProperty().set(copiedTexturePath);
            addTextureLabel.setText(Path.of(copiedTexturePath).getFileName().toString());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onDeleteSelectedSystem(){
        try{
            authViewModel.deleteSelectedSystem();
            showAuthErrorIfPresent();
        }catch(Exception e){
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEditTexture() {
        try {
            String copiedTexturePath = chooseAndCopyTextureFile();
            if (copiedTexturePath == null) {
                return;
            }

            bodyEditorViewModel.texturePathProperty().set(copiedTexturePath);
            editTextureLabel.setText(Path.of(copiedTexturePath).getFileName().toString());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void setSimulationScreen(SimulationScreen simulationScreen){
        this.simulationScreen=simulationScreen;
        applyTimeScale(timeScaleSlider.getValue());
        simulationScreen.setSizeScale(sizeScaleSlider.getValue());
    }

    private void refreshAllUi(){
        bodyEditorViewModel.refreshLists();

        focusDropdown.getItems().setAll(solarSystem.getBodyNames());
        if(focusDropdown.getValue() == null && focusDropdown.getItems().contains("Sun")){
            focusDropdown.setValue("Sun");
        }else if(!focusDropdown.getItems().contains(focusDropdown.getValue())){
            focusDropdown.setValue(focusDropdown.getItems().isEmpty() ? null : focusDropdown.getItems().get(0));
        }
        updateFocusedBodyDetails(focusDropdown.getValue());
    }

    private void updateFocusedBodyDetails(String bodyName){
        if(bodyName==null){
            clearFocusedBodyDetails();
            return;
        }
        Body body = solarSystem.getBody(bodyName);
        SolarSystemState.OrbitElements orbit = solarSystem.getOrbitElements(bodyName);

        if (body==null){
            clearFocusedBodyDetails();
            return;
        }

        massLabel.setText(Double.toString(body.getMass()));
        radiusKmLabel.setText(Double.toString(solarSystem.getBodyRadiusKm(bodyName)));
        semiMajorAxisAuLabel.setText(orbit==null? "-": Double.toString(orbit.semiMajorAxisAu()));
        eccentricityLabel.setText(orbit==null? "-": Double.toString(orbit.eccentricity()));
        inclinationDegreeLabel.setText(orbit==null? "-": Double.toString(orbit.inclinationDeg()));
        ascendingNodeDegreeLabel.setText(orbit==null? "-": Double.toString(orbit.ascendingNodeDeg()));
        argumentOfPeriapsisDegreeLabel.setText(orbit==null? "-": Double.toString(orbit.argumentOfPeriapsisDeg()));
    }
    private void clearFocusedBodyDetails() {
        massLabel.setText("-");
        radiusKmLabel.setText("-");
        semiMajorAxisAuLabel.setText("-");
        eccentricityLabel.setText("-");
        inclinationDegreeLabel.setText("-");
        ascendingNodeDegreeLabel.setText("-");
        argumentOfPeriapsisDegreeLabel.setText("-");
    }

    private void pullEditorValuesFromUi(){
        bodyEditorViewModel.massProperty().set(parseDouble(massField.getText()));
        bodyEditorViewModel.radiusKmProperty().set(parseDouble(radiusKmField.getText()));
        bodyEditorViewModel.semiMajorAxisAuProperty().set(parseDouble(semiMajorAxisAuField.getText()));
        bodyEditorViewModel.eccentricityProperty().set(parseDouble(eccentricityField.getText()));
        bodyEditorViewModel.inclinationDegProperty().set(parseDouble(inclinationDegreeField.getText()));
        bodyEditorViewModel.ascendingNodeDegProperty().set(parseDouble(ascendingNodeDegreeField.getText()));
        bodyEditorViewModel.argumentOfPeriapsisDegProperty().set(parseDouble(argumentOfPeriapsisDegreeField.getText()));
        bodyEditorViewModel.trueAnomalyDegProperty().set(parseDouble(trueAnomalyDegField.getText()));
        bodyEditorViewModel.rotationSpeedDegPerSecondProperty().set(
                parseDouble(rotationSpeedDegPerSecondField.getText())
        );

    }
    private void pushEditorValuesToUi(){
        nameField.setText(bodyEditorViewModel.nameProperty().get());
        typeDropdown.setValue(bodyEditorViewModel.typeProperty().get());
        parentDropdown.setValue(bodyEditorViewModel.parentProperty().get());
        massField.setText(Double.toString(bodyEditorViewModel.massProperty().get()));
        radiusKmField.setText(Double.toString(bodyEditorViewModel.radiusKmProperty().get()));
        semiMajorAxisAuField.setText(Double.toString(bodyEditorViewModel.semiMajorAxisAuProperty().get()));
        eccentricityField.setText(Double.toString(bodyEditorViewModel.eccentricityProperty().get()));
        ascendingNodeDegreeField.setText(Double.toString(bodyEditorViewModel.ascendingNodeDegProperty().get()));
        inclinationDegreeField.setText(Double.toString(bodyEditorViewModel.inclinationDegProperty().get()));
        argumentOfPeriapsisDegreeField.setText(Double.toString(bodyEditorViewModel.argumentOfPeriapsisDegProperty().get()));
        trueAnomalyDegField.setText(Double.toString(bodyEditorViewModel.trueAnomalyDegProperty().get()));
        colorPicker.setValue(bodyEditorViewModel.colorProperty().get());
        rotationSpeedDegPerSecondField.setText(
                Double.toString(bodyEditorViewModel.rotationSpeedDegPerSecondProperty().get())
        );

        String selectedTexture = bodyEditorViewModel.texturePathProperty().get();
        String textureLabel = selectedTexture == null || selectedTexture.isBlank()
                ? "No file selected"
                : Path.of(selectedTexture).getFileName().toString();

        addTextureLabel.setText(textureLabel);
        editTextureLabel.setText(textureLabel);
    }

    private void syncEditSlidersFromForm(){
        massSlider.setValue(Math.log10(Math.max(parseDouble(massField.getText()), 1.0) / selectedBaseMass));
        radiusKmSlider.setValue(parseDouble(radiusKmField.getText()) / selectedBaseRadiusKm);
        semiMajorAxisAuSlider.setValue(parseDouble(semiMajorAxisAuField.getText()) / selectedBaseSemiMajorAxisAu);
        eccentricitySlider.setValue(parseDouble(eccentricityField.getText()));
        ascendingNodeDegreeSlider.setValue(parseDouble(ascendingNodeDegreeField.getText()));
        inclinationDegreeSlider.setValue(parseDouble(inclinationDegreeField.getText()));
        argumentOfPeriapsisDegreeSlider.setValue(parseDouble(argumentOfPeriapsisDegreeField.getText()));
        rotationSpeedSlider.setValue(parseDouble(rotationSpeedDegPerSecondField.getText()));
    }

    private void clearAddTextureSelection(){
        addTextureLabel.setText("No file selected");
    }

    private void clearEditTextureSelection(){
        editTextureLabel.setText("No file selected");
    }

    private void showAuthErrorIfPresent(){
        String error = authViewModel.errorMessageProperty().get();
        if(error!=null&&!error.isBlank()){
            showError(error);
        }
    }
    private void showError(String errorMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private double parseDouble(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(text.trim());
    }

    private void applyTimeScale(double value){
        double timeScale = sliderToTimeScale(value);
        if(simulationScreen!=null){
            simulationScreen.setTimeScale(timeScale);
        }
        if(timeScaleLabel!=null){
            timeScaleLabel.setText(formatTimeScale(timeScale));
        }
    }

    private double sliderToTimeScale(double value) {
        double clamped = Math.max(0.0,Math.min(1.0,value));
        double exponent = Math.log(maxTimeScale/minTimeScale);
        return minTimeScale*Math.exp(exponent*clamped);
    }

    private void configureEditSlidersForSelectedBody(){
        double mass = parseDouble(massField.getText());
        double radiusKm = parseDouble(radiusKmField.getText());
        double semiMajorAxisAu = parseDouble(semiMajorAxisAuField.getText());

        double safeMass=Math.max(1.0,mass);
        massSlider.setMin(0.0);
        massSlider.setMax(safeMass*2.0);
        massSlider.setValue(safeMass);

        radiusKmSlider.setMin(1.0);
        radiusKmSlider.setMax(Math.max(radiusKm*2.0,1000));
        radiusKmSlider.setValue(Math.max(radiusKm,1.0));

        semiMajorAxisAuSlider.setMin(0.0001);
        semiMajorAxisAuSlider.setMax(Math.max(semiMajorAxisAu*2.0,100));
        semiMajorAxisAuSlider.setValue(Math.max(semiMajorAxisAu,0.0001));

        eccentricitySlider.setMin(0.0);
        eccentricitySlider.setMax(0.9999);

        inclinationDegreeSlider.setMin(-360.0);
        inclinationDegreeSlider.setMax(360.0);

        ascendingNodeDegreeSlider.setMin(0-360.0);
        ascendingNodeDegreeSlider.setMax(360.0);

        argumentOfPeriapsisDegreeSlider.setMin(-360.0);
        argumentOfPeriapsisDegreeSlider.setMax(360.0);
    }

    private String formatTimeScale(double timeScale) {
        if (timeScale < 60.0) {
            return String.format("%.2fx real time", timeScale);
        }

        double minutesPerSecond=timeScale / 60.0;
        if(timeScale < 3600){
            return String.format("%2f min/sec", minutesPerSecond);
        }

        double hoursPerSecond=timeScale / 3600;
        if(timeScale < 86400){
            return String.format("%2f hr/sec", hoursPerSecond);
        }
        double daysPerSecond=timeScale / 86400;
        return String.format("%.2f day/sec", daysPerSecond);
    }
    private String chooseAndCopyTextureFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose JPG Texture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG Images", "*.jpg", "*.jpeg")
        );

        Window window = saveButton.getScene() == null ? null : saveButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            return null;
        }

        validateTextureFile(selectedFile);

        Path textureDirectory = getTextureDirectory();
        Files.createDirectories(textureDirectory);

        Path destination = textureDirectory.resolve(UUID.randomUUID() + ".jpg");

        Files.copy(
                selectedFile.toPath(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return destination.toAbsolutePath().toString();
    }

    private void validateTextureFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            throw new IOException("Texture must be a JPG image.");
        }

        long fileSize = Files.size(file.toPath());
        if (fileSize > MAX_TEXTURE_BYTES) {
            throw new IOException("Texture must be 2 MB or smaller.");
        }

        Image image = new Image(file.toURI().toString());

        if (image.isError()) {
            throw new IOException("Could not load texture image.");
        }

        if (image.getWidth() > MAX_TEXTURE_WIDTH || image.getHeight() > MAX_TEXTURE_HEIGHT) {
            throw new IOException("Texture must be 2048x2048 or smaller.");
        }
    }

    private Path getTextureDirectory() {
        return Path.of(
                System.getProperty("user.home"),
                ".solar-system-simulation",
                "textures"
        );
    }

    private double multiplierFromSlider(double sliderValue){
        return Math.pow(10.0,sliderValue);
    }

    private String formatDouble(double value){
        return String.format("%.4f", value);
    }

    private void captureSelectedBodyBases(){
        selectedBaseMass = Math.max(bodyEditorViewModel.massProperty().get(),1.0);
        selectedBaseRadiusKm = Math.max(bodyEditorViewModel.radiusKmProperty().get(),0.0001);
        selectedBaseSemiMajorAxisAu = Math.max(bodyEditorViewModel.semiMajorAxisAuProperty().get(),0.0001);
    }
}
