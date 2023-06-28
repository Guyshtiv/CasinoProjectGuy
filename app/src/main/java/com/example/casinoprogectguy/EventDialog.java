package com.example.casinoprogectguy;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.casinoprogectguy.blackjack.BlackjackActivity;

public class EventDialog extends DialogFragment {
    private static final String TAG = "winner_dialog";
    private MediaPlayer winnerMusic;
    private int money = 0;
    private boolean soundOn = true;
    private Events currentEvent = Events.WIN;
    private OnDismissListener onDismissListener = null;
    private View.OnClickListener onClickListener = null;

    public EventDialog() {

    }

    public EventDialog(Events currentEvent, int money, boolean soundOn) {
        this.currentEvent = currentEvent;
        this.money = money;
        this.soundOn = soundOn;
    }

    public static EventDialog display(FragmentManager fragmentManager, Events currentEvent, int money, boolean soundOn) {
        EventDialog dialog = new EventDialog(currentEvent, money, soundOn);
        dialog.show(fragmentManager, TAG);
        return dialog;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, com.google.android.material.R.style.Base_Theme_AppCompat_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_event, container, false);
        TextView tvEvent = view.findViewById(R.id.tvEvent);
        TextView tvMoney = view.findViewById(R.id.tvMoney);

        switch (currentEvent) {
            case WIN:
                tvEvent.setText("You Won!");
                tvMoney.setText("You Won: " + money + "$");
                break;
            case LOSE:
                tvEvent.setText("You Lost...");
                tvMoney.setText("You Lost: " + money + "$");
                break;
            case DRAW:
                tvEvent.setText("It's a draw!");
                tvMoney.setVisibility(View.GONE);
                break;
            case SURRENDER:
                tvEvent.setText("You Surrendered");
                tvMoney.setText("Half Of Stack: " + money + "$");
                break;

        }

        if (requireActivity().getClass().equals(BlackjackActivity.class) && currentEvent != Events.SURRENDER) {
            AppCompatButton btnReturnToBetting = view.findViewById(R.id.btnReturnToBetting);
            btnReturnToBetting.setVisibility(View.VISIBLE);
            btnReturnToBetting.setOnClickListener(onClickListener);
        }

        if (soundOn) {
            if (currentEvent == Events.WIN) {
                winnerMusic = MediaPlayer.create(requireContext(), R.raw.winning_sound);
                winnerMusic.setLooping(true);
                winnerMusic.start();
            } else if (currentEvent == Events.LOSE || currentEvent == Events.SURRENDER) {
                winnerMusic = MediaPlayer.create(requireContext(), R.raw.lose_sound);
                winnerMusic.setLooping(true);
                winnerMusic.start();
            }

        }
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (soundOn && currentEvent != Events.DRAW) {
            winnerMusic.release();
        }

        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    public enum Events {
        WIN, LOSE, DRAW, SURRENDER
    }

    public interface OnDismissListener {
        void onDismiss();
    }

}
