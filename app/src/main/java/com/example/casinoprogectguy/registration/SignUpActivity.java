package com.example.casinoprogectguy.registration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.SharedPrefrencesHelper;
import com.example.casinoprogectguy.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    EditText emailSignUp, passwordSignUp, verifyPasswordSignUp, userNameSignUp;
    TextView tvFails;
    Button btnSignUp, btnSignIn;
    ImageButton userPictureImageButton;
    private FirebaseAuth mAuth;
    Dialog loadingDialog;
    Bitmap bm;
    StorageReference storageRef;
    FirebaseStorage storage;
    boolean haveImage;
    Uri urlPicture;
    SharedPreferences sp;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailSignUp = findViewById(R.id.emailSignUp);
        userNameSignUp = findViewById(R.id.userNameSignUp);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        verifyPasswordSignUp = findViewById(R.id.verifyPasswordSignUp);
        userPictureImageButton = findViewById(R.id.userPictureImageButton);
        btnSignUp = findViewById(R.id.btnToSignUp);
        btnSignIn = findViewById(R.id.btnReturnSignIn);
        tvFails = findViewById(R.id.tvFails);
        btnSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        userPictureImageButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        sp=getSharedPreferences("info",0);
        edit=sp.edit();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public void onClick(View v) {
        if (btnSignUp == v) {
            String email, password, verifiedPassword, userName;
            email = emailSignUp.getText().toString();
            password = passwordSignUp.getText().toString();
            verifiedPassword = verifyPasswordSignUp.getText().toString();
            userName = userNameSignUp.getText().toString();
            if (email.equals("") || password.equals("") || verifiedPassword.equals("")) { // בודק תקינות פרטים
                tvFails.setText("you have to enter all data");
            } else {
                // בודק תקינות דרישות
                String requirements = passedRequirements(email, password, verifiedPassword);
                if (requirements.equals("passed")) {
                    showLoadingDialog();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        //user is updated in auth
                                        //update picture
                                        if (haveImage) {//ממיר את התמונה לביטים
                                            StorageReference pictursRef = storageRef.child("profilePicturs/" + mAuth.getCurrentUser().getEmail() + "profilepicture.jpg");
                                            byte[] data = transferImageToByte();
                                            UploadTask uploadTask = pictursRef.putBytes(data);
                                            uploadTask.addOnFailureListener(new OnFailureListener() {//מוריד את התמונה לסטורג
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            Toast.makeText(SignUpActivity.this, "something went wrong", Toast.LENGTH_LONG).show();
                                                            urlPicture = null;
                                                        }
                                                    })
                                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            Toast.makeText(SignUpActivity.this, "תמונת הפרופיל שלך עודכנה", Toast.LENGTH_LONG).show();
                                                            pictursRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {//מקבלים את התמונה כ URI ואז נשים אותה התוך ה AOUTH של USER
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    setUserNameAndPicture(userName, uri);
                                                                }
                                                            });
                                                        }
                                                    });

                                        }
                                        else {
                                            //כאשר למשתמש אין תמונה אז הוא יהיה כתמונה הרגילה של משתמש באפליקציה
                                            urlPicture = Uri.parse("android.resource://com.example.casinoprogectguy/" + R.drawable.userimage);
                                            setUserNameAndPicture(userName, urlPicture);
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(SignUpActivity.this, "לא הצלחת להירשם נסה שוב", Toast.LENGTH_SHORT).show();
                                        dismissLoadingDialog();
                                    }
                                }
                            });
                } else {
                    tvFails.setText("some of the inputs are wrong: " + requirements);
                }
            }
        }
        else if (btnSignIn == v) {
            Intent it = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(it);
        } else if (userPictureImageButton == v) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
        }
    }

    public void setUserNameAndPicture(String userName, Uri userImageUrl) {
        //בפעולה זו נעדכן את פרטי המשתמש בFIREBASE AOUTHENTICATION כך שיהיה למשתמש נוח לגשת לפרטיו האישיים
        urlPicture = userImageUrl;
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .setPhotoUri(userImageUrl)
                .build();

        user.updateProfile(profileUpdates) //מעדכן את השם משתמש והתמונה של כל משתמש
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setProfileInFireStore(user); //על מנת שנוכל לדעת פרטים על משתמשים אחרים נשתמש בFIREBASE FIRESTORE ונשמור פרטים אישיים של המשתמש ללא סיסמה מכיוון שזהו חשאי לכל משתמש
                        } else {
                            Toast.makeText(SignUpActivity.this, "משהו השתבש נסה שוב", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) //
        {
            if (resultCode == RESULT_OK) {
                bm = (Bitmap) data.getExtras().get("data");
                BitmapDrawable ob = new BitmapDrawable(getResources(), bm);
                userPictureImageButton.setBackground(ob);
                Toast.makeText(this, "Your profile picture has changed successfully", Toast.LENGTH_LONG).show();
                haveImage = true;
            } else
                Toast.makeText(this, "No picture was taken", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(R.layout.dialog_loading);

        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private String passedRequirements(String email, String password, String verifyPass) {
        if (!email.contains("@")) {
            return "Not valid email";
        } else if (password.length() < 6) {
            return "Password not strong enough";
        } else if (!password.equals(verifyPass)) {
            return "Passwords are not the same";
        } else {
            return "passed";
        }
    }

    private byte[] transferImageToByte() {
        userPictureImageButton.setDrawingCacheEnabled(true);
        userPictureImageButton.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) userPictureImageButton.getBackground()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        return data;
    }

    private void setProfileInFireStore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Users").document("usersProfiles");//הכנסת הפרטים של המשתמש
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userDetails = document.getData();
                        User NewUser = new User(user.getDisplayName(), user.getEmail(),urlPicture);
                        NewUser.setMoney(50);
                        //יהיה רשום גם לכל משתמש את הכתובת של התמונת פרופיל שלו ואת כמות הכסף הדיגיטלי שלו שמשתמשים אחרים יוכלו לדעת

                        userDetails.put("" + mAuth.getCurrentUser().getEmail(), NewUser);
                        db.collection("Users").document("usersProfiles")
                                .set(userDetails)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dismissLoadingDialog();
                                        setSharedPrefrenses(NewUser);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this, "קרתה תקלה באמצע בבקשה נסו שוב", Toast.LENGTH_SHORT).show();
                                        dismissLoadingDialog();
                                    }
                                });
                    }
                }
            }
        });
    }


    private void setSharedPrefrenses(User newUser){
        SharedPrefrencesHelper sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        boolean checkValid=sharedPrefrencesHelper.addSharedPrefrences(newUser);
        if (checkValid) {
            Toast.makeText(SignUpActivity.this, "ההרשמה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(it);
        }
        else{
            Toast.makeText(SignUpActivity.this, "משהו שגוי נסה שוב", Toast.LENGTH_SHORT).show();

        }
    }
}
