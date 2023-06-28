package com.example.casinoprogectguy.winners;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.User;
import com.example.casinoprogectguy.registration.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class WinnersActivity extends AppCompatActivity {

    ArrayList<User> userArrayList = new ArrayList<User>();
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ImageView profileImageFirst,profileImageSecond,profileImageThird;
    TextView userNameSecondPlace,userNameFirstPlace,userNameThirdPlace;
    TextView moneyThirdPlace,moneyFirstPlace,moneySecondPlace;
    ListView lvCustomLayoutUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winners);
        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        profileImageFirst=findViewById(R.id.profileImageFirst);
        profileImageSecond=findViewById(R.id.profileImageSecond);
        profileImageThird=findViewById(R.id.profileImageThird);
        userNameSecondPlace=findViewById(R.id.userNameSecondPlace);
        userNameFirstPlace=findViewById(R.id.userNameFirstPlace);
        moneySecondPlace=findViewById(R.id.moneySecondPlace);
        userNameThirdPlace=findViewById(R.id.userNameThirdPlace);
        moneyThirdPlace=findViewById(R.id.moneyThirdPlace);
        moneyFirstPlace=findViewById(R.id.moneyFirstPlace);
        lvCustomLayoutUsers=findViewById(R.id.lvCustomLayoutUsers);
        getArrayList();

        setUpToolbar();

    }
    public void getTopPlayers(){
        Collections.sort(userArrayList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return Integer.compare(user2.getMoney(), user1.getMoney());
            }
        });
        setUIForTop3Players(userArrayList);
        setListView(userArrayList);

    }
    public void setUIForTop3Players(ArrayList<User> userArrayList){
        User firstUser=userArrayList.get(0);
        Glide.with(this).load(firstUser.getProfileImageUri()).into(profileImageFirst);
        moneyFirstPlace.setText(""+firstUser.getMoney());
        userNameFirstPlace.setText(firstUser.getUserName());

        User secondUser=userArrayList.get(1);
        Glide.with(this).load(secondUser.getProfileImageUri()).into(profileImageSecond);
        moneySecondPlace.setText(""+secondUser.getMoney());
        userNameSecondPlace.setText(secondUser.getUserName());

        User thirdUser=userArrayList.get(2);
        Glide.with(this).load(thirdUser.getProfileImageUri()).into(profileImageThird);
        moneyThirdPlace.setText(""+thirdUser.getMoney());
        userNameThirdPlace.setText(thirdUser.getUserName());

    }
    public void setListView(ArrayList<User> userArrayList){

        WinnersTableAdapter adapter=new WinnersTableAdapter(WinnersActivity.this,0,0,userArrayList.subList(3, userArrayList.size()));
        lvCustomLayoutUsers.setAdapter(adapter);
    }
    public void getArrayList(){
        DocumentReference docRef = db.collection("Users").document("usersProfiles");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userDetails = document.getData();
                        for (Map.Entry entry: userDetails.entrySet()){
                            Map<String, Object> UserHash = (HashMap<String, Object>) entry.getValue();
                            User user=new User ((""+UserHash.get("userName")),(""+UserHash.get("email")));
                            user.setMoney(Integer.parseInt((""+UserHash.get("money"))));
                            user.setProfileImageUri(Uri.parse(""+UserHash.get("profileImageUri")));
                            userArrayList.add(user);
                        }
                        getTopPlayers();
                    }
                    else{
                        Toast.makeText(WinnersActivity.this,"failed to update firebase money.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getMenu().removeItem(R.id.DailyMoney);

        if (mAuth.getCurrentUser()!=null){
            toolbar.getMenu().removeItem(R.id.moveToSignUp);
        }
        else{
            toolbar.getMenu().removeItem(R.id.Disconnect);
        }
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id==R.id.moveToSignUp){
                intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            }
            else if (id==R.id.Disconnect){
                mAuth.signOut();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            else if (id==R.id.action_return){
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            return true;
        });
    }


}