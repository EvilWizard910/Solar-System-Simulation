package com.example.planetsimdemo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

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

    private SolarSystem solarSystem;
    private AuthViewModel authViewModel;
    private BodyEditorViewModel bodyEditorViewModel;

    public void initialize(){
        authViewModel = new AuthViewModel(
                new FirebaseAuthenticationService(),
                new InitialConditionsRepository(new FirestoreContext().firestore()));
        bodyEditorViewModel=new BodyEditorViewModel(solarSystem);

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
        focusDropdown.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                updateFocusedBodyDetails(newValue));

        timeScaleSlider.valueProperty().addListener((obs, oldValue, newValue) -> {

        });

        sizeScaleSlider.valueProperty().addListener((obs, oldValue, newValue) -> {

        });
    }

    private void bindEditSection(){
        bodyList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            bodyEditorViewModel.selectedBodyNameProperty().set(newValue);
            bodyEditorViewModel.loadSelectedBody();
            pushEditorValuesToUi();
            syncEditSlidersFromForm();
        });
        massSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            massField.setText(Double.toString(newValue.doubleValue()));
        });
        radiusKmSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            radiusKmField.setText(Double.toString(newValue.doubleValue()));
        });
        semiMajorAxisAuSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            semiMajorAxisAuField.setText(Double.toString(newValue.doubleValue()));
        });
        eccentricitySlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            eccentricityField.setText(Double.toString(newValue.doubleValue()));
        });
        inclinationDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            inclinationDegreeField.setText(Double.toString(newValue.doubleValue()));
        });
        ascendingNodeDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            ascendingNodeDegreeField.setText(Double.toString(newValue.doubleValue()));
        });
        argumentOfPeriapsisDegreeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            argumentOfPeriapsisDegreeField.setText(Double.toString(newValue.doubleValue()));
        });
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
    }

    @FXML
    private void onDeleteSelectedBody(){
        if(!bodyEditorViewModel.removeSelectedBody()){
            showError(bodyEditorViewModel.errorMessageProperty().get());
            return;
        }
        refreshAllUi();
        clearEditTextureSelection();
    }

    @FXML
    private void onAddTexture(){

    }

    @FXML
    private void onEditTexture(){

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
        argumentOfPeriapsisDegreeField.setText(Double.toString(bodyEditorViewModel.argumentOfPeriapsisDegProperty().get()));
        trueAnomalyDegField.setText(Double.toString(bodyEditorViewModel.trueAnomalyDegProperty().get()));
        colorPicker.setValue(bodyEditorViewModel.colorProperty().get());
    }

    private void syncEditSlidersFromForm(){
        massSlider.setValue(parseDouble(massField.getText()));
        radiusKmSlider.setValue(parseDouble(radiusKmField.getText()));
        semiMajorAxisAuSlider.setValue(parseDouble(semiMajorAxisAuField.getText()));
        eccentricitySlider.setValue(parseDouble(eccentricityField.getText()));
        ascendingNodeDegreeSlider.setValue(parseDouble(ascendingNodeDegreeField.getText()));
        inclinationDegreeSlider.setValue(parseDouble(inclinationDegreeField.getText()));
        argumentOfPeriapsisDegreeSlider.setValue(parseDouble(argumentOfPeriapsisDegreeField.getText()));
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
}
