package com.example.planetsimdemo;

import javafx.beans.property.*;
/*This class will set up methods for users to sign in/out or register into Firebase. The user will have to
enter an email and password. This class will also handle errors related to Firebase authentication.
 */

public class AuthViewModel {
    private final FirebaseAuthenticationService authService;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final ObjectProperty<AuthSession> currentSession = new SimpleObjectProperty<>(null);
    private final StringProperty errorMessage = new SimpleStringProperty("");
    public final BooleanProperty signingIn = new SimpleBooleanProperty(false);

    public AuthViewModel(FirebaseAuthenticationService authService) {
        this.authService = authService;
    }
    public void SignIn(){
        errorMessage.set("");
        signingIn.set(true);
        try{
            AuthSession session = authService.signIn(email.get(),password.get());
            currentSession.set(session);
            password.set("");
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }finally {signingIn.set(false);}
    }

    public void signUp(){
        errorMessage.set("");
        signingIn.set(true);

        try {
            AuthSession session = authService.signUp(email.get(),password.get());
            currentSession.set(session);
            password.set("");
        }catch(Exception e){
            errorMessage.set(e.getMessage());
        }finally {signingIn.set(false);}
    }

    public void signOut(){
        currentSession.set(null);
        email.set("");
        password.set("");
        errorMessage.set("");
    }

    public boolean isSignedOut(){
       return currentSession.get() == null && !currentSession.get().isAuthenticated();
    }

    public StringProperty emailProperty(){return email;}
    public StringProperty passwordProperty(){return password;}
    public ObjectProperty<AuthSession> currentSessionProperty(){return currentSession;}
    public StringProperty errorMessageProperty(){return errorMessage;}
    public BooleanProperty signingInProperty(){return signingIn;}

}
