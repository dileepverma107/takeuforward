package com.takuforward.takuforward.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.takuforward.takuforward.model.AppUser;
import com.takuforward.takuforward.respository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class FirebaseUserService {

    private final Firestore firestore;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public FirebaseUserService(Firestore firestore, ObjectMapper objectMapper, UserRepository userRepository) {
        this.firestore = firestore;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initialize() {
        setupUserListener();
    }

    private void setupUserListener() {
        firestore.collection("users").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {

                switch (dc.getType()) {
                    case ADDED:

                        String userId = dc.getDocument().getId();
                        try {
                            UserRecord userRecord = FirebaseAuth.getInstance().getUser(userId);
                            Map<String, Object> dataMap= dc.getDocument().getData();
                            processNewUser(userRecord, dataMap);
                        } catch (FirebaseAuthException authException) {
                            authException.printStackTrace();
                        }
                        break;
                    case MODIFIED:
                        String updateUserId = dc.getDocument().getId();
                        try {
                            UserRecord userRecord = FirebaseAuth.getInstance().getUser(updateUserId);
                            updateUserByEmail(userRecord, dc.getDocument().getData());
                        } catch (FirebaseAuthException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case REMOVED:
                        System.out.println("Removed user: " + dc.getDocument().getData());
                        break;
                }
            }
        });
    }

    private void processNewUser(UserRecord userRecord, Map<String, Object> data) {

        AppUser existingUser = userRepository.findByEmail(userRecord.getEmail());
        if(existingUser == null) {
            AppUser user = new AppUser();
            user.setEmail(userRecord.getEmail());
            user.setName((String) data.get("name"));
            user.setProfileImage((String) data.get("profileImage"));
            userRepository.save(user);
        } else {
            System.out.println("User already Exists!");
        }
    }

    public void updateUserByEmail(UserRecord userRecord, Map<String, Object> data) {
        System.out.println("Updating user:");
        AppUser existingUser = userRepository.findByEmail(userRecord.getEmail());
        if (existingUser != null) {
            existingUser.setName((String) data.get("name"));
            existingUser.setProfileImage((String)data.get("profileImage"));
            userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User with email " + userRecord.getEmail() + " not found.");
        }
    }
}