package com.example.casinoprogectguy;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.casinoprogectguy.blackjack.BlackjackActivity;
import com.example.casinoprogectguy.luckyWheel.BetSettingsActivity;
import com.example.casinoprogectguy.notification.NotificationReceiver;
import com.example.casinoprogectguy.registration.SignUpActivity;
import com.example.casinoprogectguy.slotMachine.SlotMachineActivity;
import com.example.casinoprogectguy.winners.WinnersActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private TextView tvImageCounter, chooseGameTv;
    private ConstraintLayout clMain;
    private AppCompatButton btnGoToBlackjack, btnGoToSlotMachine, btnGoToRoulette, btnGoToWinners;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private FirebaseAuth mAuth;
    private final String[] iconNames = {
            "cherry", "coin", "diamond", "grapes", "horseshoe", "lemon", "watermelon"
    };
    private final int totalImagesNum = 52 + iconNames.length + 1;
    private int successCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chooseGameTv = findViewById(R.id.chooseGameTv);
        progressBar = findViewById(R.id.progressBar);
        tvImageCounter = findViewById(R.id.tvImageCounter);

        clMain = findViewById(R.id.clMain);

        btnGoToBlackjack = findViewById(R.id.btnGoToBlackjack);
        btnGoToBlackjack.setOnClickListener(this);

        btnGoToSlotMachine = findViewById(R.id.btnGoToSlotMachine);
        btnGoToSlotMachine.setOnClickListener(this);

        btnGoToRoulette = findViewById(R.id.btnGoToRoulette);
        btnGoToRoulette.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        btnGoToWinners = findViewById(R.id.btnGoToWinners);
        btnGoToWinners.setOnClickListener(this);
        setUpToolbar();
        setVisibilityButtons();
        if (!InternalStorage.loaded) {
            loadAllImages();
        } else {
            finishedLoading();
        }
    }

    private void loadAllImages() {
        boolean hasCards = true, hasIcons = true;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final long ONE_MEGABYTE = 1024 * 1024;

        //check for cards
        for (int i = 2; i <= 53; i++) {
            String name = i + ".png";
            String path = sharedPref.getString(name, "");
            Bitmap bitmap = InternalStorage.loadImageFromStorage(path, name);
            if (bitmap == null) {
                hasCards = false;
                break;
            }
            int value = i % 13;
            if (value == 0) value = 13;
            Card card = new Card(value, bitmap);
            InternalStorage.cards.add(card);
        }

        if (!hasCards) {
            InternalStorage.cards.clear();
            for (int i = 2; i <= 53; i++) {
                int numCard = i;
                storageRef.child("cards/" + i + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        int value = numCard % 13;
                        if (value > 10 || value == 0) value = 10;
                        Bitmap bitmap = byteArrToBitmap(bytes);
                        InternalStorage.saveToInternalStorage(MainActivity.this, numCard + ".png", bitmap);
                        Card card = new Card(value, bitmap);
                        InternalStorage.cards.add(card);
                        updateImageCounter();
                    }
                }).addOnFailureListener(e -> setError());
            }
        } else {
            successCounter += 52;
        }

        //check for back of card
        String backOfCardName = "backOfCard.png";
        String backOfCardPath = sharedPref.getString(backOfCardName, "");
        Bitmap backOfCardBitmap = InternalStorage.loadImageFromStorage(backOfCardPath, backOfCardName);

        if (backOfCardBitmap == null) {
            storageRef.child("cards/" + backOfCardName).getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                Bitmap bitmap = byteArrToBitmap(bytes);
                InternalStorage.saveToInternalStorage(MainActivity.this, backOfCardName, bitmap);
                InternalStorage.backOfCard = bitmap;
                updateImageCounter();
            }).addOnFailureListener(exception -> setError());
        } else {
            InternalStorage.backOfCard = backOfCardBitmap;
            updateImageCounter();
        }


        //check for icons
        for (String iconName : iconNames) {
            String name = iconName + ".png";
            String path = sharedPref.getString(name, "");
            Bitmap bitmap = InternalStorage.loadImageFromStorage(path, name);
            if (bitmap == null) {
                hasIcons = false;
                break;
            }
            InternalStorage.icons.add(bitmap);
        }

        if (!hasIcons) {
            InternalStorage.icons.clear();
            for (String iconName : iconNames) {
                storageRef.child("slotMachineIcons/" + iconName + ".png").getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    Bitmap bitmap = byteArrToBitmap(bytes);
                    InternalStorage.saveToInternalStorage(MainActivity.this, iconName + ".png", bitmap);
                    InternalStorage.icons.add(bitmap);
                    updateImageCounter();
                }).addOnFailureListener(e -> setError());
            }
        } else {
            successCounter += 6;
            updateImageCounter();
        }
    }

    private void updateImageCounter() {
        successCounter++;
        tvImageCounter.setText(successCounter + "/" + totalImagesNum);
        if (successCounter == totalImagesNum) finishedLoading();
    }

    private void setError() {
        progressBar.setVisibility(View.GONE);
        tvImageCounter.setText("An unexpected error occurred, not all images were successfully loaded. Please try restarting the app");
    }

    private void finishedLoading() {
        InternalStorage.loaded = true;
        progressBar.setVisibility(View.GONE);
        tvImageCounter.setVisibility(View.GONE);
        clMain.setVisibility(View.VISIBLE);
    }

    private Bitmap byteArrToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.length);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.equals(btnGoToBlackjack)) {
            intent = new Intent(this, BlackjackActivity.class);
            startActivity(intent);
        } else if (view.equals(btnGoToSlotMachine)) {
            intent = new Intent(this, SlotMachineActivity.class);
            startActivity(intent);
        } else if (view.equals(btnGoToRoulette)) {
            intent = new Intent(this, BetSettingsActivity.class);
            startActivity(intent);
        } else if (view.equals(btnGoToWinners)) {
            intent = new Intent(this, WinnersActivity.class);
            startActivity(intent);
        }

    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getMenu().removeItem(R.id.action_return);
        if (mAuth.getCurrentUser() != null) {
            toolbar.getMenu().removeItem(R.id.moveToSignUp);
        } else {
            toolbar.getMenu().removeItem(R.id.Disconnect);
            toolbar.getMenu().removeItem(R.id.DailyMoney);

        }
        toolbar.getMenu().removeItem(R.id.action_return);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            Intent intent;
            if (id == R.id.moveToSignUp) {
                intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            } else if (id == R.id.Disconnect) {
                mAuth.signOut();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.DailyMoney) {
                checkIfDoneMoney();
            }
            return true;
        });
    }

    @SuppressLint("MissingPermission")
    private void checkIfDoneMoney() {
        SharedPrefrencesHelper sharedPrefrencesHelper = new SharedPrefrencesHelper(this);

        if (sharedPrefrencesHelper.isTimeDifferenceGreaterThan4Hours(sharedPrefrencesHelper.getUserDate(), sharedPrefrencesHelper.getCurrentTime())){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Calendar calendar = Calendar.getInstance();
                //10 is for how many seconds from now you want to schedule also you can
                // create a custom instance of Calender to set on exact time
                calendar.add(Calendar.HOUR, 4);
                //יוצר CHANNEL
                createNotificationChannel();

                //שם התראה ב CHANNEL
                scheduleNotification(calendar);
                //לא צריך לשים את שתי הפעולות בקלאס נפרד כי רק שתי פעולות
            }

            sharedPrefrencesHelper.setUserDate();
            Toast.makeText(this, "You won 50 Dollars Congratulations!", Toast.LENGTH_SHORT).show();

            int numMoney = sharedPrefrencesHelper.getUserMoney();
            sharedPrefrencesHelper.setUserMoney(sharedPrefrencesHelper.getUserMoney() + 50);
            FirebaseHelper firebaseHelper = new FirebaseHelper(this);
            firebaseHelper.updateMoneyPersonInFireStore(numMoney + 50);
        } else {
            Toast.makeText(this, "You still cant get money...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setVisibilityButtons() {
        if (mAuth.getCurrentUser() == null) {
            btnGoToBlackjack.setClickable(false);
            btnGoToSlotMachine.setClickable(false);
            btnGoToRoulette.setClickable(false);
            btnGoToWinners.setClickable(false);
            chooseGameTv.setText("Sign Up First");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel() {
        String CHANNEL_ID = "casino";
        String name = "Daily Alerts";
        String des = "Channel Description A Brief";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(des);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void scheduleNotification(Calendar calendar) {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent.putExtra("titleExtra", "Free Money!");
        intent.putExtra("textExtra", "It's been 4 hours come and get you free money!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }



}