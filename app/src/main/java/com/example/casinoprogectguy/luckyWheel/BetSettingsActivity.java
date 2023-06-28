package com.example.casinoprogectguy.luckyWheel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.SharedPrefrencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class BetSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatButton btnBetOnColors, btnBetOnNumbers;
    private int maxSelections = 12;
    private List<Integer> numbers;
    private NumberAdapter adapter;
    AppCompatButton submitNumbersBtn, submitBtnColors;
    Toolbar toolbar;
    int moneyToBetOn = -1;
    String colorChosen=null;
    TextView textViewMoneyBetOnNumbers,textViewMoneyBetOnColors;
    static TextView textViewNumbersLeft;
    SeekBar seekBarlayoutOfBetNumbers, seekBarlayoutOfBetColors;
    ViewGroup layoutOfBetNumbers, layoutOfBetColors;
    RadioGroup radioGroupColors;
    int moneyUserAndMaxHeCanBetOn=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet_settings);


        setRecyclerView();

        textViewNumbersLeft = findViewById(R.id.textViewNumbersLeft);
        seekBarlayoutOfBetNumbers = findViewById(R.id.seekBarlayoutOfBetNumbers);
        textViewMoneyBetOnNumbers = findViewById(R.id.textViewMoneyBetOn);
        submitNumbersBtn = findViewById(R.id.submitBtn);
        btnBetOnColors = findViewById(R.id.btnBetOnColors);
        btnBetOnNumbers = findViewById(R.id.btnBetOnNumbers);
        seekBarlayoutOfBetColors = findViewById(R.id.seekBarlayoutOfBetColors);
        submitBtnColors = findViewById(R.id.submitBtnColors);
        radioGroupColors = findViewById(R.id.colorChoiceGroup);
        textViewMoneyBetOnColors=findViewById(R.id.textViewMoneyBetOnColors);
        submitBtnColors.setOnClickListener(this);
        submitNumbersBtn.setOnClickListener(this);
        btnBetOnColors.setOnClickListener(this);
        btnBetOnNumbers.setOnClickListener(this);
        toolbar = findViewById(R.id.toolbar);

        ///בהתחלה הNUMBERS הוא מסומן
        makeUnClickBtn(btnBetOnNumbers, btnBetOnColors);
        SharedPrefrencesHelper helper=new SharedPrefrencesHelper(this);
        moneyUserAndMaxHeCanBetOn=helper.getUserMoney();
        if (moneyUserAndMaxHeCanBetOn<10){
            returnToMainIfDontHaveMoney();
        }
        setSeekbar(10, moneyUserAndMaxHeCanBetOn, seekBarlayoutOfBetNumbers, textViewMoneyBetOnNumbers);


        //ConstraintLayout
        layoutOfBetNumbers = findViewById(R.id.layoutOfBetNumbers);
        layoutOfBetColors = findViewById(R.id.layoutOfBetColors);

        SharedPrefrencesHelper sp = new SharedPrefrencesHelper(this);
        toolbar.setSubtitle("Money: " + sp.getUserMoney() + "$");
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View view) {
        if (submitNumbersBtn == view) {
            List<Integer> selectedNumbers = adapter.getSelectedNumbers();
            if (adapter.getSelectedNumbers().size() == 12) {
                Collections.sort(selectedNumbers);
                Toast.makeText(BetSettingsActivity.this, "Selected " + selectedNumbers.size() + " numbers: " + selectedNumbers.toString(), Toast.LENGTH_SHORT).show();

                String[] arr = textViewMoneyBetOnNumbers.getText().toString().split("\\$");
                moneyToBetOn = Integer.parseInt(arr[0]);
                if (moneyToBetOn != 0) {
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                    }
                    Toast.makeText(BetSettingsActivity.this, "num of money is:" + moneyToBetOn, Toast.LENGTH_SHORT).show();
                    startGame();
                } else {
                    Toast.makeText(BetSettingsActivity.this, "need to bet on real money", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BetSettingsActivity.this, "need to choose more numbers to bet on", Toast.LENGTH_SHORT).show();
            }
        }
        else if (btnBetOnColors == view) {
            makeUnClickBtn(btnBetOnColors, btnBetOnNumbers);
            colorChosen = "";
            setRadioGroupColors();
            setSeekbar(10, moneyUserAndMaxHeCanBetOn, seekBarlayoutOfBetColors,textViewMoneyBetOnColors);
            layoutOfBetNumbers.setVisibility(View.GONE);
            layoutOfBetColors.setVisibility(View.VISIBLE);


        }
        else if (btnBetOnNumbers == view) {
            makeUnClickBtn(btnBetOnNumbers, btnBetOnColors);
            textViewNumbersLeft.setText("Choose 12 numbers to bet on");
            colorChosen = null;
            setRecyclerView();
            setSeekbar(10, moneyUserAndMaxHeCanBetOn, seekBarlayoutOfBetNumbers,textViewMoneyBetOnNumbers);
            layoutOfBetNumbers.setVisibility(View.VISIBLE);
            layoutOfBetColors.setVisibility(View.GONE);

        }
        else if (submitBtnColors == view) {
            if (radioGroupColors.getCheckedRadioButtonId() != -1) { //כלומר אם בחרו צבע
                Toast.makeText(BetSettingsActivity.this, "Selected " + colorChosen, Toast.LENGTH_SHORT).show();
                String[] arr = textViewMoneyBetOnColors.getText().toString().split("\\$");
                moneyToBetOn = Integer.parseInt(arr[0]);
                if (moneyToBetOn != 0) {
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                    }
                    Toast.makeText(BetSettingsActivity.this, "num of money is:" + moneyToBetOn, Toast.LENGTH_SHORT).show();
                    startGame();
                } else {
                    Toast.makeText(BetSettingsActivity.this, "need to bet on real money", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(BetSettingsActivity.this, "need to choose a color", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setSeekbar(int min, int max, SeekBar seekBar,TextView tvMoney) {
        seekBar.setMax(max / 10);
        boolean checkSDKLimit = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(min / 10);
        } else {
            checkSDKLimit = true;
            seekBar.setMax((max / 10) - min);

        }
        boolean finalCheckSDKLimit = checkSDKLimit;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (!finalCheckSDKLimit) {
                    tvMoney.setText(progress * 10 + "$");
                } else {
                    tvMoney.setText(((progress * 10) + min) + "$");

                }
                //  Toast.makeText(getApplicationContext(),"seekbar progress: "+progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Numbers Layouyts
    public static void removeOneNumber() {
        String string = textViewNumbersLeft.getText().toString();
        //יודעים שיש חמישה רווחים ובינהם המספר יהיה ברווח השני
        String[] stringArr = string.split(" ");
        int num = Integer.parseInt(stringArr[1]);
        stringArr[1] = "" + (num - 1);
        String result = "";
        for (String str : stringArr) {
            result += str + " ";
        }
        textViewNumbersLeft.setText(result);
    }

    public static void addOneNumber() {
        String string = textViewNumbersLeft.getText().toString();
        //יודעים שיש חמישה רווחים ובינהם המספר יהיה ברווח השני
        String[] stringArr = string.split(" ");
        int num = Integer.parseInt(stringArr[1]);
        stringArr[1] = "" + (num + 1);
        String result = "";
        for (String str : stringArr) {
            result += str + " ";
        }
        textViewNumbersLeft.setText(result);
    }

    private List<Integer> generateNumbers() {
        // Generate and return a list of 37 numbers here
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 37; i++) {
            numbers.add(i);
        }
        return numbers;
    }

    private void startGame() {
        Intent movePage = new Intent(BetSettingsActivity.this, LuckyRouletteActivity.class);
        if (colorChosen!=null) {//כלומר אם קיים COLOR לכן בחרו ב COLORS
            movePage.putExtra("betSettings", "Colors");
            movePage.putExtra("color", colorChosen);
        }
        else{
            movePage.putExtra("betSettings", "Numbers");
            movePage.putIntegerArrayListExtra("numbersList", (ArrayList<Integer>) adapter.getSelectedNumbers());
        }
        movePage.putExtra("money",moneyToBetOn);

        startActivity(movePage);

    }

    private void makeUnClickBtn(Button btnToSetUnClick, Button btnToSetClick) {
        btnToSetUnClick.setAlpha(0.65f);
        btnToSetUnClick.setClickable(false);
        btnToSetClick.setAlpha(1f);
        btnToSetClick.setClickable(true);
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        numbers = generateNumbers(); // Generate your list of numbers here
        adapter = new NumberAdapter(numbers, maxSelections, this);
        recyclerView.setAdapter(adapter);
    }

    private void setRadioGroupColors() {
        radioGroupColors.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                if (radioButton != null && radioButton.isChecked()) {
                    colorChosen = radioButton.getText().toString();
                    Toast.makeText(BetSettingsActivity.this,"color: "+colorChosen,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void returnToMainIfDontHaveMoney(){
        Toast.makeText(this,"you dont have enoph money to bet",Toast.LENGTH_SHORT).show();
        Intent it=new Intent(this, MainActivity.class);
        startActivity(it);
    }


}