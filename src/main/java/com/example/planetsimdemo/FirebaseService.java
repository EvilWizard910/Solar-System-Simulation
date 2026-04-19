package com.example.planetsimdemo;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseService {
    private static Firestore db;

    public static void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                db = FirestoreClient.getFirestore();
                return;
            }

            InputStream serviceAccount = FirebaseService.class
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                throw new RuntimeException("firebase-service-account.json not found in resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();

            System.out.println("Firebase initialized.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    public static List<BodyRecord> loadBodies(String systemId) {
        List<BodyRecord> bodies = new ArrayList<>();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("solarSystem")
                    .document(systemId)
                    .collection("bodies")
                    .get();

            List<QueryDocumentSnapshot> docs = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {
                Map<String, Object> data = doc.getData();

                BodyRecord record = new BodyRecord();
                record.id = doc.getId();
                record.name = (String) data.get("name");
                record.type = (String) data.get("type");
                record.parentId = (String) data.get("parentId");
                record.mass = ((Number) data.get("mass")).doubleValue();
                record.radiusKm = ((Number) data.get("radiusKm")).doubleValue();
                record.distanceAu = ((Number) data.get("distanceAu")).doubleValue();
                record.angleDeg = ((Number) data.get("angleDeg")).doubleValue();
                record.color = (String) data.get("color");

                bodies.add(record);
            }

            System.out.println("Loaded " + bodies.size() + " bodies from Firestore.");
            return bodies;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load bodies from Firestore", e);
        }
    }

    public static void saveBody(String systemId, String docId, BodyRecord record) {
        try {
            db.collection("solarSystem")
                    .document(systemId)
                    .collection("bodies")
                    .document(docId)
                    .set(record)
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save body: " + docId, e);
        }
    }

    public static void deleteBody(String systemId, String docId) {
        try {
            db.collection("solarSystem")
                    .document(systemId)
                    .collection("bodies")
                    .document(docId)
                    .delete()
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete body: " + docId, e);
        }
    }
}