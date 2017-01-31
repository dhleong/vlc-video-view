package net.dhleong.vlcvideoview;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * @author dhleong
 */
public class VlcVideoView extends FrameLayout
    implements IVLCVout.Callback {
    static LibVLC vlcInstance;

    private MediaPlayer player;

    private FrameLayout surfaceContainer;
    private SurfaceView playerView;
    private SurfaceView subtitlesView;

    public VlcVideoView(Context context) {
        super(context);
        init(context);
    }

    public VlcVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VlcVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VlcVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        FrameLayout surfaceFrame = surfaceContainer = new FrameLayout(context);
        surfaceFrame.setForegroundGravity(Gravity.CLIP_HORIZONTAL | Gravity.CLIP_VERTICAL);

        surfaceFrame.addView(playerView = new SurfaceView(context));
        surfaceFrame.addView(subtitlesView = new SurfaceView(context));

        addView(surfaceFrame, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // release all held resources
        release();
    }

    public void setVideoMrl(@NonNull String mrl) {
        setMedia(new Media(getVlc(), mrl));
    }

    public void setVideoUri(@NonNull Uri uri) {
        setMedia(new Media(getVlc(), uri));
    }

    private void setMedia(@NonNull final Media media) {
        // release any existing player
        release();

        media.setEventListener(new Media.EventListener() {
            @Override
            public void onEvent(Media.Event event) {
                switch (event.type) {
                case Media.Event.DurationChanged:
                    Log.d("VVV", "media.onDurationChanged: " + event.getParsedStatus());
                    break;

                case Media.Event.StateChanged:
                    Log.d("VVV", "media.StateChanged: " + media.getState());
                    break;

                default:
                    Log.i("VVV", "media.UNKNOWN: " + Integer.toHexString(event.type));
                }
            }
        });

        player = new MediaPlayer(media);
        player.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                // TODO: forward events so clients can do something with them
                switch (event.type) {
                case MediaPlayer.Event.Playing:
                    Log.v("VVV", "mediaPlayer.PLAYING");
                    break;

                case MediaPlayer.Event.Paused:
                    Log.v("VVV", "mediaPlayer.PAUSED");
                    break;

                case MediaPlayer.Event.EndReached:
                    Log.v("VVV", "mediaPlayer.END_REACHED");
                    break;

                case MediaPlayer.Event.EncounteredError:
                    Log.w("VVV", "mediaPlayer.ERROR");
                    break;

                case MediaPlayer.Event.Vout:
                    Log.v("VVV", "mediaPlayer.VOUT");
                    break;

                case MediaPlayer.Event.Buffering:
                case MediaPlayer.Event.TimeChanged:
                case MediaPlayer.Event.PositionChanged:
                    break;

                default:
                    Log.v("VVV", "mediaPlayer.UNKNOWN? " + Integer.toHexString(event.type));
                }
            }
        });

        IVLCVout vlcOut = player.getVLCVout();
        if (vlcOut.areViewsAttached()) {
            player.stop();
            vlcOut.detachViews();
        }

        vlcOut.addCallback(this);

        vlcOut.setVideoView(playerView);
        vlcOut.setSubtitlesView(subtitlesView);
        vlcOut.attachViews();
        player.setVideoTrackEnabled(true);
        player.play();
    }

    public void play() {
        MediaPlayer player = this.player;
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    public void pause() {
        MediaPlayer player = this.player;
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    /**
     * Skip some number of millis; may be negative
     * @param skipMillis
     */
    public void skip(long skipMillis) {
        MediaPlayer player = this.player;
        if (player != null) {
            setTime(player.getTime() + skipMillis);
        }
    }

    /**
     * Jump to a specific time in millis
     * @param timeInMillis
     */
    public void setTime(long timeInMillis) {
        MediaPlayer player = this.player;
        if (player != null) {
            player.setTime(timeInMillis);
        }
    }

    public void stop() {
        MediaPlayer player = this.player;
        if (player != null) {
            player.stop();
        }
    }

    /**
     * Stop and clean up; normally called for you when
     * the view is detached from the Window
     */
    public void release() {
        stop();

        MediaPlayer player = this.player;
        if (player != null && !player.isReleased()) {
            player.release();
        }

        this.player = null;
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout,
            int width, int height,
            int visibleWidth, int visibleHeight,
            int specifiedAspectNumerator,
            int specifiedAspectDenominator) {
        Log.v("VVV", "*** NEW LAYOUT!!! " + width + "x" + height);
        View decorView = getDecorView();
        int screenWidth = decorView.getWidth();
        int screenHeight = decorView.getHeight();

        // sanity check
        if (screenWidth * screenHeight == 0) {
            Log.e("VVV", "Unexpected screen size: " + screenWidth + "x" + screenHeight);
            return;
        }

        vlcVout.setWindowSize(screenWidth, screenHeight);

        if (width * height == 0) {
            // vlc is handling it internally; we do nothing
            ViewGroup.LayoutParams playerParams = playerView.getLayoutParams();
            playerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.setLayoutParams(playerParams);

            ViewGroup.LayoutParams containerParams = surfaceContainer.getLayoutParams();
            containerParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            containerParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceContainer.setLayoutParams(containerParams);
            return;
        }

        // okay, we're in charge
        // compute the aspect ratio
        float videoWidth;
        if (specifiedAspectNumerator == specifiedAspectDenominator) {
            // no indication about the density, assuming 1:1
            videoWidth = visibleWidth;
        } else {
            // use the specified aspect ratio
            videoWidth = visibleWidth *
                 specifiedAspectNumerator /
                    (float) specifiedAspectDenominator;
        }
        float aspect = videoWidth / (float) visibleHeight;

        // compute display aspect ratio
        float floatScreenWidth = screenWidth;
        float floatScreenHeight = screenHeight;
        float displayAspect = floatScreenWidth / floatScreenHeight;
        if (displayAspect < aspect) {
            floatScreenHeight = floatScreenWidth / aspect;
        } else {
            floatScreenWidth = floatScreenHeight * aspect;
        }

        // set display size
        ViewGroup.LayoutParams playerParams = playerView.getLayoutParams();
        playerParams.width = (int) Math.ceil(floatScreenWidth * width / (float) visibleWidth);
        playerParams.height = (int) Math.ceil(floatScreenHeight * height / (float) visibleHeight);
        playerView.setLayoutParams(playerParams);
        subtitlesView.setLayoutParams(playerParams);

        // set frame size (crop if necessary)
        ViewGroup.LayoutParams surfaceParams = surfaceContainer.getLayoutParams();
        surfaceParams.width = (int) Math.floor(floatScreenWidth);
        surfaceParams.height = (int) Math.floor(floatScreenHeight);
        surfaceContainer.setLayoutParams(surfaceParams);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        // nop
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
        // nop
    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        // nop
    }

    private @Nullable Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }

            context = ((ContextWrapper) context).getBaseContext();
        }

        return null;
    }

    private View getDecorView() {
        Activity act = getActivity();
        if (act != null) {
            return act.getWindow().getDecorView();
        }

        throw new IllegalStateException("Couldn't get DecorView");
    }

    LibVLC getVlc() {
        LibVLC existing = vlcInstance;
        if (existing != null) return existing;

        // should we cache this?
        return vlcInstance = new LibVLC(
            getContext().getApplicationContext(),
            VLCOptions.get());
    }
}
