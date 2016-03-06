package nl.svendubbeld.car.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import nl.svendubbeld.car.R;
import nl.svendubbeld.car.service.NotificationListenerService;

public class MediaControlView extends FrameLayout implements MediaSessionManager.OnActiveSessionsChangedListener {

    private TextView mMediaTitleView;
    private TextView mMediaArtistView;
    private TextView mMediaAlbumView;
    private ImageView mMediaArtView;
    private ImageView mMediaPlayView;
    private ProgressBar mMediaPlayProgressView;
    private ImageView mMediaVolDownView;
    private ImageView mMediaPrevView;
    private ImageView mMediaNextView;
    private ImageView mMediaVolUpView;

    private MediaController mMediaController = null;
    private MediaSessionManager mMediaSessionManager;

    public MediaControlView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_media_control, this);

        mMediaTitleView = (TextView) findViewById(R.id.media_title);
        mMediaArtistView = (TextView) findViewById(R.id.media_artist);
        mMediaAlbumView = (TextView) findViewById(R.id.media_album);
        mMediaArtView = (ImageView) findViewById(R.id.media_art);
        mMediaPlayView = (ImageView) findViewById(R.id.media_play);
        mMediaPlayProgressView = (ProgressBar) findViewById(R.id.media_play_progress);
        mMediaVolDownView = ((ImageView) findViewById(R.id.media_vol_down));
        mMediaPrevView = ((ImageView) findViewById(R.id.media_prev));
        mMediaNextView = ((ImageView) findViewById(R.id.media_next));
        mMediaVolUpView = ((ImageView) findViewById(R.id.media_vol_up));

        mMediaVolDownView.setOnClickListener(mMediaControlsListener);
        mMediaPrevView.setOnClickListener(mMediaControlsListener);
        mMediaPlayView.setOnClickListener(mMediaControlsListener);
        mMediaNextView.setOnClickListener(mMediaControlsListener);
        mMediaVolUpView.setOnClickListener(mMediaControlsListener);
    }

    public void startMediaUpdates() {
        mMediaSessionManager = (MediaSessionManager) getContext().getSystemService(Context.MEDIA_SESSION_SERVICE);

        ComponentName notificationListener = new ComponentName(getContext(), NotificationListenerService.class);

        try {
            List<MediaController> controllers = mMediaSessionManager.getActiveSessions(notificationListener);
            onActiveSessionsChanged(controllers);

            mMediaSessionManager.addOnActiveSessionsChangedListener(this, notificationListener);
        } catch (SecurityException e) {
            Log.w("NotificationListener", "No Notification Access");
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.dialog_notification_access_title)
                    .setMessage(R.string.dialog_notification_access_message)
                    .setPositiveButton(R.string.dialog_notification_access_positive, (dialog, which) -> {
                        getContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_notification_access_negative, (dialog, which) -> dialog.dismiss())
                    .show();

        }
    }

    public void stopMediaUpdates() {
        if (mMediaSessionManager != null) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
        }

        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            Log.d("MediaController", "MediaController removed");
        }
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (controllers.size() > 0) {

            if (mMediaController != null) {
                if (!controllers.get(0).getSessionToken().equals(mMediaController.getSessionToken())) {
                    // Detach current controller
                    mMediaController.unregisterCallback(mMediaCallback);
                    Log.d("MediaController", "MediaController removed");
                    mMediaController = null;

                    // Attach new controller
                    mMediaController = controllers.get(0);
                    mMediaController.registerCallback(mMediaCallback);
                    mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                    mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                    Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
                }
            } else {
                // Attach new controller
                mMediaController = controllers.get(0);
                mMediaController.registerCallback(mMediaCallback);
                mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                Log.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
            }

            mMediaPlayView.setImageTintList(ColorStateList.valueOf(getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor}).getColor(0, getResources().getColor(R.color.white))));
        } else {

            mMediaArtistView.setText("");
            mMediaAlbumView.setText("");
            mMediaTitleView.setText(R.string.media_idle);
            mMediaArtView.setImageResource(R.drawable.bg_default_album_art);

            Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            PackageManager pm = getContext().getPackageManager();
            ResolveInfo info = pm.resolveActivity(i, 0);

            Drawable icon = info.loadIcon(pm);
            mMediaPlayView.setPadding(20, 20, 20, 20);
            mMediaPlayView.setImageDrawable(icon);

            mMediaPlayView.setImageTintList(null);
        }
    }

    private MediaController.Callback mMediaCallback = new MediaController.Callback() {

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);

            if (mMediaTitleView != null && metadata != null) {
                mMediaArtView.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitleView.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtistView.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbumView.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));
            }
        }

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if (mMediaPlayView != null) {

                // Update play/pause button
                switch (state.getState()) {
                    case PlaybackState.STATE_BUFFERING:
                    case PlaybackState.STATE_CONNECTING:
                        mMediaPlayView.setVisibility(View.GONE);
                        mMediaPlayProgressView.setVisibility(View.VISIBLE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_pause);
                        break;
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlayView.setVisibility(View.VISIBLE);
                        mMediaPlayProgressView.setVisibility(View.GONE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_pause);
                        break;
                    default:
                        mMediaPlayView.setVisibility(View.VISIBLE);
                        mMediaPlayProgressView.setVisibility(View.GONE);
                        mMediaPlayView.setImageResource(R.drawable.ic_av_play_arrow);
                        break;
                }
            }
        }
    };

    private View.OnClickListener mMediaControlsListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mMediaController != null) {
                // Handle media controls
                switch (v.getId()) {
                    case R.id.media_vol_down:
                        mMediaController.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                        break;
                    case R.id.media_prev:
                        mMediaController.getTransportControls().skipToPrevious();
                        break;
                    case R.id.media_play:
                        if (mMediaController.getPlaybackState() != null) {
                            switch (mMediaController.getPlaybackState().getState()) {
                                case PlaybackState.STATE_BUFFERING:
                                case PlaybackState.STATE_CONNECTING:
                                    mMediaPlayView.setVisibility(View.GONE);
                                    mMediaPlayProgressView.setVisibility(View.VISIBLE);
                                case PlaybackState.STATE_PLAYING:
                                    mMediaController.getTransportControls().pause();
                                    break;
                                default:
                                    mMediaController.getTransportControls().play();
                                    break;
                            }
                        } else {
                            mMediaController.getTransportControls().play();
                        }
                        break;
                    case R.id.media_next:
                        mMediaController.getTransportControls().skipToNext();
                        break;
                    case R.id.media_vol_up:
                        mMediaController.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                        break;
                }
            } else {

                Intent mediaIntent = new Intent(Intent.ACTION_MAIN);
                mediaIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
                mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(mediaIntent);

            }
        }
    };
}
