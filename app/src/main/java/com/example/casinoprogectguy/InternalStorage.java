package com.example.casinoprogectguy;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InternalStorage {

    //all pictures will be loaded into the app once launched
    public static boolean loaded = false;

    //stack of 52 cards
    public static Stack<Card> cards = new Stack<>();


    public static Bitmap backOfCard;

    //array of 7 slot machine icons
    public static List<Bitmap> icons = new ArrayList<>();

    public static void saveToInternalStorage(Activity activity, String name, Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(activity.getApplicationContext());

        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath = new File(directory,name + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(name, directory.getAbsolutePath());
        editor.apply();
    }

    public static Bitmap loadImageFromStorage(String path, String name) {
        Bitmap bitmap = null;
        try {
            if (!path.equals("")) {
                File f = new File(path, name + ".png");
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
