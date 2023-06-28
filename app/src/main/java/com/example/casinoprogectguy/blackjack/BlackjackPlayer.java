package com.example.casinoprogectguy.blackjack;


import com.example.casinoprogectguy.Card;

public class BlackjackPlayer {
    private int total = 0;
    private int aceNum = 0;

    public void addCard(Card card) {
        if (card.getValue() == 1) {
            aceNum++;
            total += 11;
        } else {
            total += Math.min(card.getValue(), 10);
        }

        if (total > 21 && aceNum > 0) {
            total -= 10;
            aceNum--;
        }
    }

    public int getTotal() {
        return total;
    }

    public void clear() {
        total = 0;
        aceNum = 0;
    }
}
