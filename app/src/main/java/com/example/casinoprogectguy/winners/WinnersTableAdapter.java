package com.example.casinoprogectguy.winners;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.casinoprogectguy.R;
import com.example.casinoprogectguy.User;

import java.util.List;

public class WinnersTableAdapter extends ArrayAdapter<User> {
    private Context context;
    List<User> objects;
    View view;
    ImageView ivUser;

    public WinnersTableAdapter(Context context, int resource, int textViewResourceId, List<User> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        view = layoutInflater.inflate(R.layout.rowlistviewlayout, parent, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSubTitle = view.findViewById(R.id.tvSubTitle);
        TextView tvMoney = view.findViewById(R.id.tvMoney);
        ivUser = view.findViewById(R.id.ivUser);
        User user = objects.get(position);
        if (user.getUserName().equals("")){
            tvMoney.setText(""+user.getMoney());
            tvTitle.setText(user.getEmail());
            return view;
        }
        else {
            tvMoney.setText(""+user.getMoney());
            tvTitle.setText(user.getUserName());
            tvSubTitle.setText(user.getEmail());
            Glide.with(context).load(user.getProfileImageUri()).into(ivUser);
            return view;
        }
    }
}
