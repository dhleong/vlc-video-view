package net.dhleong.vlcvideoview;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

/**
 * @author dhleong
 */
public class VlcVideoView extends FrameLayout {
    static LibVLC vlcInstance;

    private MediaPlayer player;

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
        addView(playerView = new SurfaceView(context));
        addView(subtitlesView = new SurfaceView(context));
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

    LibVLC getVlc() {
        LibVLC existing = vlcInstance;
        if (existing != null) return existing;

        // should we cache this?
        return vlcInstance = new LibVLC(
            getContext().getApplicationContext(),
            VLCOptions.get());
    }
}
