package com.example.casinoprogectguy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Card  implements Comparable<Card> {
    //ace = 1
    //two = 2, three = 3...
    //jack = 11, queen = 12, king = 13

    private final int value;
    private final Bitmap bitmap;

    public Card(int value, Bitmap bitmapCard) {
        this.value = value;
        this.bitmap=bitmapCard;
    }

    public int getValue() {
        return value;
    }


    public Bitmap getBitmapCard() {
      return bitmap;
    }

    //משמש לSORT של ARRAYLIST
    @Override
    public int compareTo(Card card) {
        int cardValue = card.getValue();
        return value - cardValue;
    }


}
