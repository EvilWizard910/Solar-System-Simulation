package com.example.planetsimdemo;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicInteger;
/*This class will set up methods for users to sign in/out or register into Firebase. The user will have to
enter an email and password. This class will also handle errors related to Firebase authentication.
 */

public class AuthViewModel {
    private final FirebaseAuthenticationService authService;
    private final InitialConditionsRepository repository;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final ObjectProperty<AuthSession> currentSession = new SimpleObjectProperty<>(null);
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    public final StringProperty saveName = new SimpleStringProperty("");

    private final ObservableList<String> savedSystems = FXCollections.observableArrayList();
    private final StringProperty selectedSavedSystem = new SimpleStringProperty();

    public AuthViewModel(FirebaseAuthenticationService authService, InitialConditionsRepository repository) {
        this.authService = authService;
        this.repository = repository;
    }

    public void signIn(){
        errorMessage.set("");
        busy.set(true);
        try{
            AuthSession session = authService.signIn(email.get(),password.get());
            currentSession.set(session);
            password.set("");
            refreshSavedSystems();
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }finally {busy.set(false);}
    }

    public void signUp(){
        errorMessage.set("");
        busy.set(true);

        try {
            AuthSession session = authService.signUp(email.get(),password.get());
            currentSession.set(session);
            password.set("");
            refreshSavedSystems();
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }finally {busy.set(false);}
    }

    public void signOut(){
        currentSession.set(null);
        email.set("");
        password.set("");
        saveName.set("");
        selectedSavedSystem.set(null);
        savedSystems.clear();
        errorMessage.set("");
    }

    public void refreshSavedSystems(){
        AuthSession session = currentSession.get();
        if(session != null || !session.isAuthenticated()){
            savedSystems.clear();
            selectedSavedSystem.set(null);
            return;
        }
        try{savedSystems.setAll(repository.listSystems(session.uid()));
            if(savedSystems.isEmpty()){
            selectedSavedSystem.set(null);}
            else if(!savedSystems.contains(selectedSavedSystem.get())){
                selectedSavedSystem.set(savedSystems.get(0));
            }
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }
    }

    public void saveCurrentSession(SolarSystemState state){
        AuthSession session = currentSession.get();
        if(session == null||!session.isAuthenticated()){
            errorMessage.set("Sign in to save");
            return;
        }
        String name=saveName.get()==null?"":saveName.get().trim();
        if(name.isEmpty()){
            errorMessage.set("Please enter a name for the save");
            return;
        }
        try{
            repository.saveSystem(session.uid(), name,state);
            refreshSavedSystems();
            selectedSavedSystem.set(name);
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }
    }

    public SolarSystemState loadSelectedSystem()throws Exception{
        AuthSession session = currentSession.get();
        if(session == null||!session.isAuthenticated()){
           throw new IllegalStateException("Sign in to load");
        }
        String systemName=selectedSavedSystem.get();
        if(systemName==null || systemName.isBlank()){
            throw new IllegalStateException("Select a save system");}
        return repository.loadSystem(session.uid(),systemName);
    }

    public boolean isSignedOut(){
        return currentSession.get() != null&&currentSession.get().isAuthenticated();
    }

    public StringProperty emailProperty(){return email;}
    public StringProperty passwordProperty(){return password;}
    public StringProperty saveNameProperty(){return saveName;}
    public ObjectProperty<AuthSession> currentSessionProperty(){return currentSession;}
    public StringProperty errorMessageProperty(){return errorMessage;}
    public BooleanProperty busyProperty(){return busy;}
    public ObservableList<String> getSavedSystems() {return savedSystems;}
    public StringProperty selectedSavedSystemProperty(){return selectedSavedSystem;}
}
