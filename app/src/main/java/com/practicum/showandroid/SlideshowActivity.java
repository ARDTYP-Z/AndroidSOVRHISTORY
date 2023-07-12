package com.practicum.showandroid;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SlideshowActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private SlideshowAdapter slideshowAdapter;
    private List<Uri> images;
    private Timer slideshowTimer;

    private int currentPosition = 0;
    private boolean isSlideshowRunning = false;
    private boolean isTouchEnabled = true;

    private static final int VOLUME_DOWN_CLICK_COUNT = 3;
    private int volumeDownClickCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        // Блокировка альбомного режима
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Разрешение только книжного режима
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        viewPager = findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        images = new ArrayList<Uri>();
        images.addAll((List<Uri>) getIntent().getSerializableExtra("images"));

        slideshowAdapter = new SlideshowAdapter(this, images, getIntent().getLongExtra("slideshowDelay", 5000));
        viewPager.setAdapter(slideshowAdapter);

        long delay = getIntent().getLongExtra("slideshowDelay", 5000);
        startSlideshow(delay);
    }

    private void startSlideshow(long delay) {
        if (slideshowTimer != null) {
            slideshowTimer.cancel();
            slideshowTimer = null;
        }

        slideshowTimer = new Timer();
        slideshowTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currentPosition == images.size() - 1) {
                            viewPager.setCurrentItem(0, true);
                        } else {
                            viewPager.setCurrentItem(currentPosition + 1, true);
                        }
                    }
                });
            }
        }, delay, delay);

        isSlideshowRunning = true;
    }


    private void stopSlideshow() {
        if (slideshowTimer != null) {
            slideshowTimer.cancel();
            slideshowTimer = null;
        }

        isSlideshowRunning = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeDownClickCounter++;
            if (volumeDownClickCounter == VOLUME_DOWN_CLICK_COUNT && isSlideshowRunning) {
                stopSlideshow();
                finish();
                return true;
            }
        } else {
            volumeDownClickCounter = 0;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSlideshowRunning && !isTouchEnabled) {
            return true; // Блокируем обработку событий касания
        }
        return super.dispatchTouchEvent(ev);
    }

    private void disableNavigationBars() {
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(flags);
    }

    private void enableNavigationBars() {
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(flags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        disableNavigationBars();
    }

    @Override
    protected void onPause() {
        super.onPause();
        enableNavigationBars();
    }

}
