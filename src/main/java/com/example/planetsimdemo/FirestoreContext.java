package com.example.planetsimdemo;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FirestoreContext {

    public Firestore firestore() {
        try {
            if(FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getResourceAsStream("/key.json");
                if(serviceAccount == null) {
                    throw new IOException("Missing key.json file");
                }
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return FirestoreClient.getFirestore();
    }


}