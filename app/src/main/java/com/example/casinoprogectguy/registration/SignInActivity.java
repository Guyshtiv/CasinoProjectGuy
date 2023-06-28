package com.example.casinoprogectguy.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    EditText Email, Password;
    TextView tvFails;
    Button btnMoveToSignUp, btnSubmit;
    private FirebaseAuth mAuth;
    Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Email = findViewById(R.id.emailSignIn);
        Password = findViewById(R.id.passwordSignIn);
        tvFails = findViewById(R.id.tvFails);
        btnMoveToSignUp = findViewById(R.id.btnMoveToSignUp);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);
        btnMoveToSignUp.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
        }
    }


    @Override
    public void onClick(View v) {
        if (btnSubmit == v) {
            String emailSignIn = Email.getText().toString();
            String passwordSignIn = Password.getText().toString();
            if (emailSignIn.equals("") || passwordSignIn.equals("")) {
                tvFails.setText("you have to enter all data");
            } else {
                showLoadingDialog();
                mAuth.signInWithEmailAndPassword(emailSignIn, passwordSignIn)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "התחברת בהצלחה", Toast.LENGTH_SHORT).show();
                                    dismissLoadingDialog();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent it = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(it);
                                } else {
                                    dismissLoadingDialog();
                                    Toast.makeText(SignInActivity.this, "ההתחברות נכשלה נסה שוב", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }

        } else if (btnMoveToSignUp == v) {
            Intent it = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(it);
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
}