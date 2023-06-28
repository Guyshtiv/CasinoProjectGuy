package com.example.casinoprogectguy.luckyWheel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.casinoprogectguy.R;

import java.util.ArrayList;
import java.util.List;

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.NumberViewHolder> {

    private List<Integer> numbers;
    private List<Integer> selectedNumbers;
    private int maxSelections;
    private Context context;

    public NumberAdapter(List<Integer> numbers, int maxSelections,Context context) {
        this.numbers = numbers;
        this.selectedNumbers = new ArrayList<>();
        this.maxSelections = maxSelections;
        this.context=context;
    }

    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_number, parent, false);
        return new NumberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final NumberViewHolder holder, int position) {
        final int number = numbers.get(position);
        holder.numberText.setText(String.valueOf(number));
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedNumbers.contains(number)) {
                    if (selectedNumbers.size() >= maxSelections) {
                        Toast.makeText(context,"you cant select more items",Toast.LENGTH_SHORT).show();
                    } else {
                        BetSettingsActivity.removeOneNumber();
                        selectedNumbers.add(number);
                        holder.constraintLayout.setBackgroundResource(R.drawable.layout_checked);
                    }
                } else {
                    selectedNumbers.remove(Integer.valueOf(number));
                    BetSettingsActivity.addOneNumber();
                    holder.constraintLayout.setBackgroundResource(R.drawable.checkbox_background);

                }
            }
        });
        if (selectedNumbers.contains(number)) {
            holder.constraintLayout.setBackgroundResource(R.drawable.layout_checked);
        } else {
            holder.constraintLayout.setBackgroundResource(R.drawable.checkbox_background);
        }
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    public List<Integer> getSelectedNumbers() {
        return selectedNumbers;
    }

    static class NumberViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout constraintLayout;
        TextView numberText;

        NumberViewHolder(View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            numberText=itemView.findViewById(R.id.numberText);

        }
    }
}