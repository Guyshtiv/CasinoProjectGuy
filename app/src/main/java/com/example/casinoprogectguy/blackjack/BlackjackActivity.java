package com.example.casinoprogectguy.blackjack;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.example.casinoprogectguy.Card;
import com.example.casinoprogectguy.EventDialog;
import com.example.casinoprogectguy.FirebaseHelper;
import com.example.casinoprogectguy.InternalStorage;
import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.SharedPrefrencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class BlackjackActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private LinearLayout llDealerCards, llGameButtons, llPreGameButtons, llPlayerCards;
    private AppCompatButton btnHit, btnStand, btnSurrender, btnStartBlackJack;
    private EditText etBetAmount;
    private TextView tvDealer, tvPlayer;
    private Stack<Card> deck;
    private int money = 100, betAmount = 0, stack = 0;
    private final BlackjackPlayer player = new BlackjackPlayer(), dealer = new BlackjackPlayer();
    private boolean soundOn = true;
    private final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(130, ViewGroup.LayoutParams.MATCH_PARENT);
    private ImageView blankCard;
    private SoundPool soundPool;
    private int soundId;
    private final List<ImageView> currentCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blackjack);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .build();
        soundId = soundPool.load(this, R.raw.card_sound, 1);

        SharedPrefrencesHelper sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        money=sharedPrefrencesHelper.getUserMoney();
        setUpCardDisplays();
        setUpPreGame();
        setUpGameButtons();
        setUpToolbar();
    }

    private void setUpPreGame() {
        etBetAmount = findViewById(R.id.etBetAmount);
        btnStartBlackJack = findViewById(R.id.btnStartBlackJack);
        btnStartBlackJack.setOnClickListener(view -> checkIfCanStart());
    }

    private void setUpGameButtons() {
        btnHit = findViewById(R.id.btnHit);
        btnHit.setOnClickListener(view -> actionHit());

        btnStand = findViewById(R.id.btnStand);
        btnStand.setOnClickListener(view -> actionStand());

        btnSurrender = findViewById(R.id.btnSurrender);
        btnSurrender.setOnClickListener(view -> actionSurrender());
    }

    @Override
    public void onClick(View view) {
        if (view.equals(btnHit)) {
            actionHit();
        } else if (view.equals(btnStand)) {
            actionStand();
        } else if (view.equals(btnSurrender)){
            actionSurrender();
        }

        view.setEnabled(false);
        new Handler().postDelayed(() -> view.setEnabled(true),1000);
    }

    private void setUpCardDisplays() {
        llGameButtons = findViewById(R.id.llGameButtons);
        llPreGameButtons = findViewById(R.id.llPreGameButtons);
        llPlayerCards = findViewById(R.id.llPlayerCards);
        llDealerCards = findViewById(R.id.llDealerCards);

        tvDealer = findViewById(R.id.tvDealer);
        tvPlayer = findViewById(R.id.tvPlayer);
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar);
        initSubtitle();
        toolbar.setOnMenuItemClickListener(item -> {
            soundOn = !soundOn;
            if (soundOn) {
                item.setIcon(R.drawable.volume_on_icon);
            } else {
                item.setIcon(R.drawable.volume_off_icon);
            }
            return true;
        });

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void initSubtitle() {
        toolbar.setSubtitle("Money: " + money + "$ - Stack: " + stack + "$");
    }

    private void checkIfCanStart() {
        String stBetAmount = etBetAmount.getText().toString();
        if (!stBetAmount.equals("")) {
            betAmount = Integer.parseInt(stBetAmount);
            if (betAmount <= money && betAmount > 0) {
                money -= betAmount;
                stack = betAmount;
                toolbar.setNavigationIcon(null);
                startGame();
            }
            setMoneyToSharedAndFirebase(money);
        }
    }

    private void startGame() {
        removeCards();
        deck = (Stack<Card>) InternalStorage.cards.clone();
        Collections.shuffle(deck);

        initSubtitle();
        llPreGameButtons.setVisibility(View.GONE);
        llGameButtons.setVisibility(View.VISIBLE);
        dealPlayer();
        dealDealer();
        addAllCards();

        tvPlayer.setText(player.getTotal() + " / 21");
        tvDealer.setText(dealer.getTotal() + " / 21");
    }

    private void dealDealer() {
        Card card = deck.pop();
        dealer.addCard(card);
        ImageView dealerCard = createImageView(card.getBitmapCard());

        blankCard = createImageView(InternalStorage.backOfCard);

        currentCards.add(dealerCard);
        currentCards.add(blankCard);
    }

    private void dealPlayer() {
        for (int i = 0; i < 2; i++) {
            Card card = deck.pop();
            player.addCard(card);
            ImageView playerCard = createImageView(card.getBitmapCard());
            currentCards.add(playerCard);
        }
    }
    private void addAllCards() {
        if (currentCards.isEmpty()) return;

        ImageView iv = currentCards.get(0);
        if (currentCards.size() > 2) {
            addToLayout(llPlayerCards, iv);
        } else {
            addToLayout(llDealerCards, iv);
        }
        currentCards.remove(0);
        new Handler().postDelayed(this::addAllCards, 750);

    }
    private void actionHit() {
        Card card = deck.pop();
        player.addCard(card);
        ImageView playerCard = createImageView(card.getBitmapCard());
        addToLayout(llPlayerCards, playerCard);
        tvPlayer.setText(player.getTotal() + " / 21");

        new Handler().postDelayed(() -> {
            if (player.getTotal() > 21) {
                //lose
                EventDialog dialog = EventDialog.display(getSupportFragmentManager(), EventDialog.Events.LOSE, betAmount, soundOn);
                dialog.setOnDismissListener(this::loseGame);
                dialog.setOnClickListener(v -> {
                    returnToBetting();
                    dialog.setOnDismissListener(null);
                    dialog.dismiss();
                });

                stack -= betAmount;
                initSubtitle();

            }
        }, 750);
    }

    private void actionStand() {
        llDealerCards.removeView(blankCard);
        Card card = deck.pop();
        dealer.addCard(card);
        ImageView dealerCard = createImageView(card.getBitmapCard());
        addToLayout(llDealerCards, dealerCard);

        new Handler().postDelayed(() -> {
            if (dealer.getTotal() < 17) actionStand();
            else checkWin();
        }, 750);

        tvDealer.setText(dealer.getTotal() + " / 21");
    }
    private void checkWin() {
        new Handler().postDelayed(() -> {
            if (dealer.getTotal() == player.getTotal()) {
                //draw
                EventDialog dialog = EventDialog.display(getSupportFragmentManager(), EventDialog.Events.DRAW, 0, soundOn);
                dialog.setOnDismissListener(this::startGame);
                dialog.setOnClickListener(v -> {
                    returnToBetting();
                    dialog.setOnDismissListener(null);
                    dialog.dismiss();
                });


            } else if (dealer.getTotal() <= 21 && dealer.getTotal() > player.getTotal()) {
                //lose
                EventDialog dialog = EventDialog.display(getSupportFragmentManager(), EventDialog.Events.LOSE, betAmount, soundOn);
                dialog.setOnDismissListener(this::loseGame);
                dialog.setOnClickListener(v -> {
                    returnToBetting();
                    dialog.setOnDismissListener(null);
                    dialog.dismiss();
                });

                stack -= betAmount;
                initSubtitle();

            } else {
                //win
                EventDialog dialog = EventDialog.display(getSupportFragmentManager(), EventDialog.Events.WIN, betAmount, soundOn);
                dialog.setOnDismissListener(this::startGame);
                dialog.setOnClickListener(v -> {
                    returnToBetting();
                    dialog.setOnDismissListener(null);
                    dialog.dismiss();
                });

                stack += betAmount;
                initSubtitle();
            }
        }, 250);
    }
    private void actionSurrender() {
        //auto lose, get 0.5 of stack back
        EventDialog dialog = EventDialog.display(getSupportFragmentManager(), EventDialog.Events.SURRENDER, stack / 2, soundOn);
        dialog.setOnDismissListener(() -> {
            stack /= 2;
            returnToBetting();
        });
    }

    private void loseGame() {
        if (stack == 0) {
            //kick from current game
            returnToBetting();
        } else {
            startGame();
        }
    }

    private ImageView createImageView(Bitmap bitmap) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(lp);

        return imageView;
    }

    private void addToLayout(LinearLayout linearLayout, ImageView imageView) {
        linearLayout.addView(imageView);

        if (soundOn) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    private void returnToBetting() {
        llPreGameButtons.setVisibility(View.VISIBLE);
        llGameButtons.setVisibility(View.GONE);
        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.back_icon, getTheme()));

        money += stack;
        stack = 0;
        etBetAmount.setText(betAmount + "");
        setMoneyToSharedAndFirebase(money);
        removeCards();
        initSubtitle();

        tvPlayer.setText("Your Cards");
        tvDealer.setText("Dealer's Cards");
    }

    private void removeCards() {
        llDealerCards.removeAllViews();
        llPlayerCards.removeAllViews();
        dealer.clear();
        player.clear();
    }

    private void setMoneyToSharedAndFirebase(int money){
        SharedPrefrencesHelper sharedPrefrencesHelper=new SharedPrefrencesHelper(this);
        FirebaseHelper firebaseHelper=new FirebaseHelper(this);
        sharedPrefrencesHelper.setUserMoney(money);
        firebaseHelper.updateMoneyPersonInFireStore(money);
    }
}