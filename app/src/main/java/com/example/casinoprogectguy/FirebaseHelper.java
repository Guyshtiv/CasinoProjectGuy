package com.example.casinoprogectguy;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Context context;



    public FirebaseHelper(Context context) {
        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        this.context=context;
    }


    public void updateMoneyPersonInFireStore(int newMoney) {
        DocumentReference docRef = db.collection("Users").document("usersProfiles");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userDetails = document.getData();
                        if (userDetails.containsKey(mAuth.getCurrentUser().getEmail())) {
                            Map<String, Object> UserHash = (Map) userDetails.get(mAuth.getCurrentUser().getEmail());
                            User newUser=new User ((""+UserHash.get("userName")),(""+UserHash.get("email")));
                            newUser.setMoney(newMoney);
                            newUser.setProfileImageUri(mAuth.getCurrentUser().getPhotoUrl());
                            userDetails.put(mAuth.getCurrentUser().getEmail(),newUser);
                            db.collection("Users").document("usersProfiles")
                                    .set(userDetails)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,"failed to update firebase money",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                    else{
                        Toast.makeText(context,"failed to update firebase money.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }




}
