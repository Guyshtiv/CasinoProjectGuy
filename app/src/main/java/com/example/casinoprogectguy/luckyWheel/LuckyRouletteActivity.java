package com.example.casinoprogectguy.luckyWheel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.casinoprogectguy.FirebaseHelper;
import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.SharedPrefrencesHelper;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LuckyRouletteActivity extends AppCompatActivity implements View.OnClickListener {
    // sectors of our wheel (look at the image to see the sectors)
    private static final String[] sectors = { "32 red", "15 black","19 red", "4 black", "21 red", "2 black", "25 red", "17 black", "34 red", "6 black", "27 red","13 black", "36 red", "11 black", "30 red", "8 black", "23 red", "10 black", "5 red", "24 black", "16 red", "33 black", "1 red", "20 black", "14 red", "31 black", "9 red", "22 black", "18 red", "29 black", "7 red", "28 black", "12 red", "35 black", "3 red", "26 black", "0 green"};
    private static final Random RANDOM = new Random();
    private int degree = 0, degreeOld = 0;
    // We have 37 sectors on the wheel, we divide 360 by this value to have angle for each sector
    // we divide by 2 to have a half sector
    private static final float HALF_SECTOR = 360f / 37f / 2f;
    AppCompatButton spinBtn,changeSettings;
    TextView betSettings,textView;
    Toolbar toolbar;
    ImageView wheel;
    String betOption;
    String result="";
    int numMoneyToBetOn;
    ArrayList<Integer> numbersList;
    String colorChosen;
    SharedPrefrencesHelper sharedPrefrencesHelper;
    FirebaseHelper fireBaseHelper;
    int moneyUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_roulette);
        spinBtn=findViewById(R.id.spinBtn);
        spinBtn.setOnClickListener(this);
        changeSettings=findViewById(R.id.changeSettings);
        changeSettings.setOnClickListener(this);
        wheel=findViewById(R.id.wheel);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        textView=findViewById(R.id.textView);
        betSettings=findViewById(R.id.betSettings);
        sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        fireBaseHelper=new FirebaseHelper(this);
        Intent it=getIntent();
        betOption=it.getStringExtra("betSettings");
        if (betOption!=null){
            if (betOption.equals("Numbers")){
                numbersList=it.getIntegerArrayListExtra("numbersList");
                betSettings.setText("Numbers - "+numbersList.toString());
            }
            else{
                colorChosen=it.getStringExtra("color");
                betSettings.setText("Colors - "+colorChosen);

            }
            numMoneyToBetOn=it.getIntExtra("money",0);
            textView.append(" "+numMoneyToBetOn);
        }
        moneyUser=getMoneyOfPerson();
        updateMoneyUI(moneyUser);

    }

    @Override
    public void onClick(View view) {
        if (view==spinBtn){
            spin(spinBtn);
        }
        else if(changeSettings==view){
            Intent movePage =new Intent(this, BetSettingsActivity.class);
            startActivity(movePage);
        }
    }
    public void spin(View v) {
        degreeOld = degree % 360;

        degree = RANDOM.nextInt(360) + 1440;

        RotateAnimation rotateAnim = new RotateAnimation(degreeOld, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(5500);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // we empty the result text view when the animation start
                result="";
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // we display the correct sector pointed by the triangle at the end of the rotate animation
                result=(getSector(360 - (degree % 360)));
                changeEndResults();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // we start the animation
        wheel.startAnimation(rotateAnim);
    }
    private String getSector(int degrees) {
        int i = 0;
        String text = null;

        do {
            // start and end of each sector on the wheel
            float start = HALF_SECTOR * (i * 2 + 1);
            float end = HALF_SECTOR * (i * 2 + 3);

            if (degrees >= start && degrees < end) {


                text = sectors[i];
            }

            i++;


        } while (text == null  &&  i < sectors.length);

        return text;
    }

    private void changeEndResults(){
        int moneyPerson=getMoneyOfPerson();

        if (betOption.equals("Numbers")){
            String result=this.result;
            String[] resultArr=result.split(" ");
            int number=Integer.parseInt(resultArr[0]);
            if (numbersList.contains(number)){
                moneyPerson+=numMoneyToBetOn;
                updateMoneyUser(moneyPerson);
                updateThatUserWon(false);
            }
            else if (number==0){
                moneyPerson+=numMoneyToBetOn*4;
                updateMoneyUser(moneyPerson);
                updateThatUserWon(true);
            }
            else{
                moneyPerson-=numMoneyToBetOn;
                lostRoulette(moneyPerson);
            }
        }
        else{
            String result=this.result;
            String[] resultArr=result.split(" ");
            String color=resultArr[1];
            if (color.toLowerCase().equals(colorChosen.toLowerCase())){
                moneyPerson+=numMoneyToBetOn;
                updateMoneyUser(moneyPerson);
                updateThatUserWon(false);
            }
            else if (color.equals("green")){
                moneyPerson+=numMoneyToBetOn*4;
                updateMoneyUser(moneyPerson);
                updateThatUserWon(true);
            }
            else{
                moneyPerson-=numMoneyToBetOn;
                lostRoulette(moneyPerson);
            }

        }
        checkIfCanContinue();
    }
    private int getMoneyOfPerson(){//להשתמש ב SHared prefrences
        return sharedPrefrencesHelper.getUserMoney();
    }
    private void updateMoneyUser(int moneyPerson){//לעדכן את ה FIREBASE והSHARED PREFRENCES
        sharedPrefrencesHelper.setUserMoney(moneyPerson);
        fireBaseHelper.updateMoneyPersonInFireStore(moneyPerson);
        //updateMoneyInUI
        updateMoneyUI(moneyPerson);

    }

    private void updateThatUserWon(boolean green){
        if (green){
            Toast.makeText(this, "congragulations you go to the green!!  \nyou money is quadrupled ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "congragulations you won the roulete", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateMoneyUI(int money){
        toolbar.setSubtitle("Money: " + money + "$");
    }
    private void lostRoulette(int moneyUser){
        Toast.makeText(this, "ohh pal you lost the roulette", Toast.LENGTH_SHORT).show();
        updateMoneyUser(moneyUser);


    }
    private void checkIfCanContinue(){
        int numMoney=getMoneyOfPerson();
        if (numMoney<numMoneyToBetOn){
            if (numMoney<10){
                Toast.makeText(this,"cant continue to bet at all",Toast.LENGTH_SHORT).show();
                Intent it=new Intent(this, MainActivity.class);
                startActivity(it);
            }
            else{
                Toast.makeText(this,"change bet money or get more money",Toast.LENGTH_SHORT).show();
                Intent it=new Intent(this,BetSettingsActivity.class);
                startActivity(it);
            }
        }

    }

}