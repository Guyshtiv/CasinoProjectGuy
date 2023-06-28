package com.example.casinoprogectguy.slotMachine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.casinoprogectguy.InternalStorage;
import com.example.casinoprogectguy.R;

public class SlotMachineColumn implements Runnable {
    private final Context context;
    private final RecyclerView recyclerView;
    public final ColumnAdapter adapter;
    private int count = 1;
    private final Handler handler = new Handler();
    private final int speedScroll = 50;
    private boolean on = false;
    private SlotMachineFinishedListener listener = null;
    public SlotMachineColumn(RecyclerView recyclerView, Context context, int pos) {
        this.recyclerView = recyclerView;
        recyclerView.addOnItemTouchListener(new RecyclerViewDisabler());
        this.context = context;

        adapter = new ColumnAdapter(context, pos);
        recyclerView.setAdapter(adapter);

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.setLayoutManager(new CenterItemLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        recyclerView.getMeasuredHeight()
                ));

                recyclerView.scrollToPosition(1);
            }
        });
    }

    public void setListener(SlotMachineFinishedListener listener) {
        this.listener = listener;
    }

    public void start() {
        count = 1;
        recyclerView.scrollToPosition(count);

        if (!on) {
            handler.postDelayed(this, speedScroll);
            on = true;
        }
    }

    @Override
    public void run() {
        if (count < adapter.getItemCount() - 1) {
            recyclerView.smoothScrollToPosition(count++);
            handler.postDelayed(this, speedScroll);
            on = false;
        } else if (count == adapter.getItemCount() - 1) {
            count++;
            if (listener != null) {
                listener.slotMachineFinished(SlotMachineColumn.this);
            }
        }

    }

    public static class ColumnAdapter extends RecyclerView.Adapter<ColumnAdapter.ViewHolder> {
        private final Context context;
        private int count;
        private static final int ICON_NUM = InternalStorage.icons.size();
        private final Bitmap[] icons = new Bitmap[ICON_NUM];
        public int result;

        public ColumnAdapter(Context context, int pos) {
            this.context = context;

            switch (pos) {
                case 1:
                    count = 8 * ICON_NUM;
                    break;
                case 2:
                    count = 10 * ICON_NUM;
                    break;
                case 3:
                    count = 12 * ICON_NUM;
                    break;
            }

            for (int i = 0; i < ICON_NUM; i++) {
                icons[i] = InternalStorage.icons.get(i);
            }
            /*icons[0] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.cherry, context.getTheme());
            icons[1] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.coin, context.getTheme());
            icons[2] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.diamond, context.getTheme());
            icons[3] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.grapes, context.getTheme());
            icons[4] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.horseshoe, context.getTheme());
            icons[5] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.lemon, context.getTheme());
            icons[6] = ResourcesCompat.getDrawable(context.getResources(), R.drawable.watermelon, context.getTheme());*/
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater.from(context).inflate( R.layout.icon_holder, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int rand = (int) (Math.random() * ((ICON_NUM-1) + 1));

            if (position == count - 2) {
                result = rand;
            }

            holder.imageView.setImageBitmap(icons[rand]);
        }

        @Override
        public int getItemCount() {
            return count;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }

    public class CenterItemLayoutManager extends LinearLayoutManager {
        private final int parentHeight;
        private final float reduce;
        public CenterItemLayoutManager(
                final Context context,
                final int orientation,
                final int parentHeight) {
            super(context, orientation, false);
            this.parentHeight = parentHeight;

            float dip = 35f;
            Resources r = context.getResources();
            reduce = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip,
                    r.getDisplayMetrics()
            );
        }

        @Override
        public int getPaddingTop() {
            return Math.round(parentHeight / 2f - reduce);
        }

        @Override
        public int getPaddingBottom() {
            return getPaddingTop();
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(context) {
                private static final float SPEED = 40f;// Change this value (default=25f)
                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return SPEED / displayMetrics.densityDpi;
                }
            };
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }
    }

    public static class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface SlotMachineFinishedListener {
        void slotMachineFinished(SlotMachineColumn column);
    }
}
