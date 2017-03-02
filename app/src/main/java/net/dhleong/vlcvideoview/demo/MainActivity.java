package net.dhleong.vlcvideoview.demo;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.dhleong.vlcvideoview.VlcVideoView;

import java.util.Collections;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity
        extends AppCompatActivity
        implements VlcVideoView.OnCompletionListener,
                   VlcVideoView.OnErrorListener,
                   VlcVideoView.OnPreparedListener, VlcVideoView.OnLoadingStateChangedListener {

    static final String TAG = "VVV-Demo";

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    ProgressBar loading;
    VlcVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loading = (ProgressBar) findViewById(R.id.loading);
        mContentView = videoView = (VlcVideoView) findViewById(R.id.video);

        videoView.setOnCompletionListener(this);
        videoView.setOnErrorListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnLoadStateChangedListener(this);

        // for AndroidTV or something:
        videoView.setOnKeyInterceptListener(new VlcVideoView.OnKeyInterceptListener() {
            @Override
            public boolean onInterceptKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        Log.v(TAG, "BACK");
                        videoView.skip(-5000);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        Log.v(TAG, "FORWARD");
                        videoView.skip(5000);
                        break;
                    }
                }
                return false;
            }
        });

        final String[] URLS = {
            "http://archive.org/download/BigBuckBunny_328/BigBuckBunny.avi",
            "http://commondatastorage.googleapis.com/android-tv/" +
                "Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4"
        };

        // start loading; we'll hit "play" when it's prepared
        videoView.setVideoUri(Uri.parse(URLS[0]),
            // this option lets us start at a specific time (in seconds)
            Collections.singletonList(":start-time=60"));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onCompletion(VlcVideoView view) {
        Toast.makeText(view.getContext(), "Complete!", Toast.LENGTH_LONG).show();
        Log.v(TAG, "*** Complete!");
    }

    @Override
    public void onError(VlcVideoView view) {
        Toast.makeText(view.getContext(), "*** ERROR!!!", Toast.LENGTH_LONG).show();
        Log.e(TAG, "*** ERROR!");
    }

    @Override
    public void onPrepared(VlcVideoView view) {
        Log.v(TAG, "PREPARED!");
        loading.setVisibility(View.GONE);
        videoView.play();
    }

    @Override
    public void onLoadingStateChanged(VlcVideoView view, boolean isLoading) {
        loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
