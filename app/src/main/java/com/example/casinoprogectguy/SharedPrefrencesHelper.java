package com.example.casinoprogectguy;


import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SharedPrefrencesHelper {
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;
    private FirebaseAuth mAuth;
    Context context;



    public SharedPrefrencesHelper(Context context) {
        this.context=context;
        sp = context.getSharedPreferences("info", 0);
        edit = sp.edit();
        mAuth = FirebaseAuth.getInstance();
    }


    public boolean addSharedPrefrences(User newUser) {
        try {
            String emails = sp.getString("emails", "");//מוסיף את האימייל והנקודות לshered כי התמונה נוספה כבר
            String points = sp.getString("money", "");
            String dates = sp.getString("dates", "");
            emails = emails + newUser.getEmail() + "#";
            points = points + newUser.getMoney() + "#";
            dates = dates + " " + "#";
            edit.putString("emails", emails);
            edit.putString("money", points);
            edit.putString("dates", dates);
            edit.commit();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    public int getUserMoney() {
        try {
            String emails = sp.getString("emails", "");
            String points = sp.getString("money", "");
            String[] emailsArr = emails.split("#");
            String[] pointsArr = points.split("#");
            int index = Arrays.asList(emailsArr).indexOf("" + mAuth.getCurrentUser().getEmail());
            if (index != -1) {
                if (Integer.parseInt(pointsArr[index]) != -1) {
                    return Integer.parseInt(pointsArr[index]);
                }
                else {
                    Toast.makeText(context,"error in get money from shared pref",Toast.LENGTH_SHORT).show();
                    return -1;
                }
            }
            return -1;
        }
        catch (Exception e){
            Toast.makeText(context,"error in get money catch! from shared pref",Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
    public boolean setUserMoney(int money) {
        try {
            String emails = sp.getString("emails", "");// משנה בsered pref  את הנקודות העדכניות של המשתמש
            String points = sp.getString("money", "");
            String[] emailsArr = emails.split("#");
            String[] moneyArr = points.split("#");
            int index = Arrays.asList(emailsArr).indexOf("" + mAuth.getCurrentUser().getEmail());
            moneyArr[index] = "" + money;//משנה את המקום של המשתמש ב shered ושם את המספר נקודות החדש
            String moneyNew = "";
            for (int i = 0; i < emailsArr.length; i++) {//רץ על כל המערך נקודות החדש וממיר אותו לסטרינג עם # בין לבין
                moneyNew = moneyNew + moneyArr[i] + "#";
            }
            edit.putString("money", moneyNew);
            edit.commit();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public String getUserDate() {
        try {
            String emails = sp.getString("emails", "");
            String dates = sp.getString("dates", "");
            String[] emailsArr = emails.split("#");
            String[] datesArr = dates.split("#");
            int index = Arrays.asList(emailsArr).indexOf("" + mAuth.getCurrentUser().getEmail());
            if (index != -1) {
                return datesArr[index];
            }
                else {
                Toast.makeText(context, "error in get money from shared pref", Toast.LENGTH_SHORT).show();
                return "";
            }


        }
        catch (Exception e){
            Toast.makeText(context,"error in get money catch! from shared pref",Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    public String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; //  חודשים נספרים מ 0-11 אז נוסיף 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return String.format(Locale.getDefault(), "%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute);
    }
    public boolean setUserDate(){
        try {
            String date=getCurrentTime();
            String emails = sp.getString("emails", "");// משנה בsered pref  את הזמן
            String dates = sp.getString("dates", "");
            String[] emailsArr = emails.split("#");
            String[] datesArr = dates.split("#");
            int index = Arrays.asList(emailsArr).indexOf("" + mAuth.getCurrentUser().getEmail());
            datesArr[index] = "" + date;//משנה את המקום של המשתמש ב shered ושם את המספר נקודות החדש
            String datesNew = "";
            for (int i = 0; i < emailsArr.length; i++) {//רץ על כל המערך הזמן החדש וממיר אותו לסטרינג עם # בין לבין
                datesNew = datesNew + datesArr[i] + "#";
            }
            edit.putString("dates", datesNew);
            edit.commit();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
    public boolean isValidDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean isTimeDifferenceGreaterThan4Hours(String dateString1, String dateString2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setLenient(false);
            Date date1,date2;
            try {
                date1 = sdf.parse(dateString1);
            }
            catch (Exception e){
                return true;
            }
            try {
                date2 = sdf.parse(dateString2);
            }
            catch (Exception e){
                return false;
            }
            long diffInMillis = Math.abs(date1.getTime() - date2.getTime());
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            return diffInHours > 4;
        } catch (Exception e) {
            return false;
        }
    }
}

