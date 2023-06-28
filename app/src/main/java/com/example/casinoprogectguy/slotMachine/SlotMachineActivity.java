package com.example.casinoprogectguy.slotMachine;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.example.casinoprogectguy.EventDialog;
import com.example.casinoprogectguy.FirebaseHelper;
import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.SharedPrefrencesHelper;

public class SlotMachineActivity extends AppCompatActivity implements SlotMachineColumn.SlotMachineFinishedListener, View.OnClickListener, Runnable {
    private SlotMachineColumn column1, column2, column3;
    private final int[] columnResults = new int[3];
    private AppCompatButton btnSpin, btnReduceBet, btnIncreaseBet;
    private EditText etDisplayBet;
    private Toolbar toolbar;
    private MediaPlayer backgroundSlotMachineSound;
    private SoundPool soundPool;
    private int soundId;
    private boolean soundOn = true;
    private boolean gameOn = false;
    private int money = 200;
    private int currentBet = 0;
    private TextView tvSpin;
    private Handler handler;
    private int tvCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slotmachine);

        SharedPrefrencesHelper sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        money= sharedPrefrencesHelper.getUserMoney();
        setUpMachineColumns();
        setUpToolbar();
        setUpButtons();
        setUpMusic();
        setUpSpinView();
    }



    private void setUpMachineColumns() {
        column1 = new SlotMachineColumn(findViewById(R.id.column1), this, 1);
        column1.setListener(this);
        column2 = new SlotMachineColumn(findViewById(R.id.column2), this, 2);
        column2.setListener(this);
        column3 = new SlotMachineColumn(findViewById(R.id.column3), this, 3);
        column3.setListener(this);
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            soundOn = !soundOn;
            if (soundOn) {
                item.setIcon(R.drawable.volume_on_icon);

                if (!backgroundSlotMachineSound.isPlaying() && gameOn) {
                    backgroundSlotMachineSound.start();
                }
            } else {
                item.setIcon(R.drawable.volume_off_icon);

                if (backgroundSlotMachineSound.isPlaying() && gameOn) {
                    backgroundSlotMachineSound.pause();
                }
            }
            return true;
        });
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(SlotMachineActivity.this, MainActivity.class);
            startActivity(intent);
        });
        toolbar.setSubtitle("Money: " + money + "$");
    }

    private void setUpButtons() {
        btnReduceBet = findViewById(R.id.btnReduceBet);
        btnReduceBet.setOnClickListener(this);
        btnIncreaseBet = findViewById(R.id.btnIncreaseBet);
        btnIncreaseBet.setOnClickListener(this);

        btnSpin = findViewById(R.id.btnSpin);
        btnSpin.setOnClickListener(view -> {
            if (reduceBetFromMoney()) {
                column1.start();
                column2.start();
                column3.start();
                gameOn = true;
                btnSpin.setEnabled(false);
                if (soundOn) {
                    backgroundSlotMachineSound.start();
                }
            }
        });

        etDisplayBet = findViewById(R.id.etDisplayBet);
    }

    private void setUpMusic() {
        backgroundSlotMachineSound = MediaPlayer.create(this, R.raw.slot_machine_sound);
        backgroundSlotMachineSound.setLooping(true);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .build();
        soundId = soundPool.load(this, R.raw.ka_ching_sound, 1);
    }

    private void setUpSpinView() {
        tvSpin = findViewById(R.id.tvSpin);
        handler = new Handler();
        handler.postDelayed(this, 500);
    }

    @Override
    public void run() {
        tvCounter++;
        if (tvCounter % 2 == 0) {
            tvSpin.setTextColor(Color.WHITE);
        } else {
            tvSpin.setTextColor(ResourcesCompat.getColor(getResources(), R.color.american_yellow, getTheme()));
        }
        handler.postDelayed(this, 500);
    }

    private boolean reduceBetFromMoney() {
        String stCurrentBet = etDisplayBet.getText().toString();
        if (!stCurrentBet.equals("")) {
            currentBet = Integer.parseInt(stCurrentBet);
            if (currentBet > 0 && currentBet <= money) {
                money -= currentBet;
                toolbar.setSubtitle("Money: " + money + "$");
                setMoneyToSharedAndFirebase(money);
                return true;
            }
        }
        return false;
    }

    @Override
    public void slotMachineFinished(SlotMachineColumn column) {
        if (column.equals(column1)) {
            columnResults[0] = column.adapter.result;
        } else if (column.equals(column2)) {
            columnResults[1] = column.adapter.result;
        } else {
            gameOn = false;
            btnSpin.setEnabled(true);
            if (backgroundSlotMachineSound.isPlaying()) {
                backgroundSlotMachineSound.pause();
            }

            columnResults[2] = column.adapter.result;

            if (columnResults[0] == columnResults[1] &&
                    columnResults[1] == columnResults[2]) {
                int moneyWon = currentBet * 4;
                EventDialog.display(getSupportFragmentManager(), EventDialog.Events.WIN, moneyWon, soundOn);
                money += moneyWon;
                toolbar.setSubtitle("Money: " + money + "$");
                setMoneyToSharedAndFirebase(money);
            }
            else if (columnResults[0] == columnResults[1] ||
                    columnResults[1] == columnResults[2] ||
                    columnResults[0] == columnResults[2]) {

                int moneyWon = currentBet * 2;
                EventDialog.display(getSupportFragmentManager(), EventDialog.Events.WIN, moneyWon, soundOn);
                money += moneyWon;
                toolbar.setSubtitle("Money: " + money + "$");
                setMoneyToSharedAndFirebase(money);

            }

        }

        if (soundOn) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void onClick(View view) {
        if (etDisplayBet.getText().toString().equals("")) {
            currentBet=0;
        }
        else {
            currentBet = Integer.parseInt(etDisplayBet.getText().toString());
        }
            final int changeRate = 10;
            if (view.equals(btnIncreaseBet)) {
                if (currentBet <= (money - changeRate)) {
                    currentBet += changeRate;
                }
            } else if (view.equals(btnReduceBet)) {
                if (currentBet > changeRate) {
                    currentBet -= changeRate;
                } else {
                    currentBet = 0;
                }
            }

            etDisplayBet.setText(currentBet + "");

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(this);
    }

    private void setMoneyToSharedAndFirebase(int money){
        SharedPrefrencesHelper sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        FirebaseHelper firebaseHelper=new FirebaseHelper(this);
        sharedPrefrencesHelper.setUserMoney(money);
        firebaseHelper.updateMoneyPersonInFireStore(money);
    }
}