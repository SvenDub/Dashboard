/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.svendubbeld.car.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.svendubbeld.car.Log;
import nl.svendubbeld.car.R;
import nl.svendubbeld.car.service.NotificationListenerService;

/**
 * Activity that shows media controls.
 */
public class MediaActivity extends Activity
        implements MediaSessionManager.OnActiveSessionsChangedListener, SeekBar.OnSeekBarChangeListener {

    /**
     * "pref_key_show_media"
     */
    private boolean mPrefShowMedia = true;

    // Current track
    private TextView mMediaAlbum;
    private ImageView mMediaArt;
    private TextView mMediaArtist;
    private TextView mMediaTitle;

    // Next track
    private TextView mMediaUpNextArtist;
    private TextView mMediaUpNextTitle;

    // Controls
    private ImageView mMediaNext;
    private ImageView mMediaPlay;
    private ProgressBar mMediaPlayProgress;
    private ImageView mMediaPrev;
    private ImageView mMediaVolDown;
    private ImageView mMediaVolUp;

    // Seek bar
    private TextView mMediaTimeEnd;
    private TextView mMediaTimePosition;
    private SeekBar mMediaTimeSeekBar;

    private MediaController mMediaController = null;
    private MediaSessionManager mMediaSessionManager;

    private SharedPreferences mSharedPref;
    private Timer mTimer;
    private TimerTask mUpdatePositionTask;
    private UpdatePositionRunnable mUpdatePositionRunnable = new UpdatePositionRunnable();

    /**
     * Callback for the MediaController.
     */
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {

        @Override
        public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
            super.onAudioInfoChanged(playbackInfo);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);

            if ((mMediaArt != null) && (metadata != null)) {

                // Update media container
                mMediaArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtist.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbum.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));

                updateQueue(mMediaController.getQueue(), mMediaController.getPlaybackState());

                // Update position
                mUpdatePositionTask.run();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if ((mMediaPlay != null) && (state != null)) {

                // Update play/pause button
                switch (state.getState()) {
                    case PlaybackState.STATE_BUFFERING:
                    case PlaybackState.STATE_CONNECTING:
                        mMediaPlay.setVisibility(View.GONE);
                        mMediaPlayProgress.setVisibility(View.VISIBLE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_pause);
                        break;
                    case PlaybackState.STATE_PLAYING:
                        mMediaPlay.setVisibility(View.VISIBLE);
                        mMediaPlayProgress.setVisibility(View.GONE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_pause);
                        break;
                    default:
                        mMediaPlay.setVisibility(View.VISIBLE);
                        mMediaPlayProgress.setVisibility(View.GONE);
                        mMediaPlay.setImageResource(R.drawable.ic_av_play_arrow);
                        break;
                }

                updateQueue(mMediaController.getQueue(), state);

                // Update position
                mUpdatePositionTask.run();
            }
        }

        @Override
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);

            updateQueue(queue, mMediaController.getPlaybackState());
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }
    };
    /**
     * OnClickListener for the media controls.
     */
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
                        switch (mMediaController.getPlaybackState().getState()) {
                            case PlaybackState.STATE_BUFFERING:
                            case PlaybackState.STATE_CONNECTING:
                                mMediaPlay.setVisibility(View.GONE);
                                mMediaPlayProgress.setVisibility(View.VISIBLE);
                            case PlaybackState.STATE_PLAYING:
                                mMediaController.getTransportControls().pause();
                                break;
                            default:
                                mMediaController.getTransportControls().play();
                                break;
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

                Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
                startActivity(i);

            }
        }
    };

    /**
     * Updates the display of the queue.
     *
     * @param queue The queue.
     * @param state The playback state.
     */
    private void updateQueue(List<MediaSession.QueueItem> queue, PlaybackState state) {
        if (state != null) {
            long queueId = state.getActiveQueueItemId();

            if (queueId != MediaSession.QueueItem.UNKNOWN_ID) {

                int i;
                for (i = 0; queue.get(i).getQueueId() != queueId; i++) ;
                MediaSession.QueueItem nextItem = queue.get(i + 1);

                mMediaUpNextTitle.setText(nextItem.getDescription().getTitle());
                mMediaUpNextArtist.setText(nextItem.getDescription().getSubtitle());
            }
        }
    }

    /**
     * Called when the list of active {@link MediaController MediaControllers} changes.
     *
     * @param controllers List of active MediaControllers
     */
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
        }
    }

    /**
     * Sets the layout and starts task for updating the seek bar.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in {@link #onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make all system bars transparent and draw behind them
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Set layout
        setContentView(R.layout.activity_media);

        // Get views
        mMediaArt = ((ImageView) findViewById(R.id.media_art));
        mMediaTimePosition = ((TextView) findViewById(R.id.media_time_position));
        mMediaTimeSeekBar = ((SeekBar) findViewById(R.id.media_time_seek_bar));
        mMediaTimeEnd = ((TextView) findViewById(R.id.media_time_end));
        mMediaTitle = ((TextView) findViewById(R.id.media_title));
        mMediaArtist = ((TextView) findViewById(R.id.media_artist));
        mMediaAlbum = ((TextView) findViewById(R.id.media_album));
        mMediaVolDown = ((ImageView) findViewById(R.id.media_vol_down));
        mMediaPrev = ((ImageView) findViewById(R.id.media_prev));
        mMediaPlay = ((ImageView) findViewById(R.id.media_play));
        mMediaPlayProgress = ((ProgressBar) findViewById(R.id.media_play_progress));
        mMediaNext = ((ImageView) findViewById(R.id.media_next));
        mMediaVolUp = ((ImageView) findViewById(R.id.media_vol_up));
        mMediaUpNextTitle = ((TextView) findViewById(R.id.media_up_next_title));
        mMediaUpNextArtist = ((TextView) findViewById(R.id.media_up_next_artist));

        mMediaTimeSeekBar.setOnSeekBarChangeListener(this);
        mMediaVolDown.setOnClickListener(mMediaControlsListener);
        mMediaPrev.setOnClickListener(mMediaControlsListener);
        mMediaPlay.setOnClickListener(mMediaControlsListener);
        mMediaNext.setOnClickListener(mMediaControlsListener);
        mMediaVolUp.setOnClickListener(mMediaControlsListener);

        mMediaPlay.setImageTintList(ColorStateList.valueOf(getTheme().obtainStyledAttributes(new int[]{R.attr.cardBackgroundColor}).getColor(0, getResources().getColor(R.color.white))));

        mMediaTitle.setSelected(true);

        // Timer for seek bar
        mTimer = new Timer();
        mUpdatePositionTask = new TimerTask() {
            public void run() {
                if (mMediaController != null) {
                    PlaybackState state = mMediaController.getPlaybackState();
                    MediaMetadata metadata = mMediaController.getMetadata();
                    if ((state != null) && (metadata != null) && (state.getState() == 3)) {
                        long rawPosition = state.getPosition();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
                        String position = dateFormat.format(new Date(rawPosition));

                        long rawEnd = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                        String end = "-" + dateFormat.format(new Date(rawEnd - rawPosition));
                        int progress = (int) (100.0f * ((float) rawPosition / (float) rawEnd));
                        mUpdatePositionRunnable.setParams(position, progress, end);
                        runOnUiThread(mUpdatePositionRunnable);
                    }
                }
            }
        };

        mTimer.scheduleAtFixedRate(mUpdatePositionTask, 100l, 1000l);
    }

    /**
     * Detaches listeners.
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (mPrefShowMedia) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
            if (mMediaController != null) {
                mMediaController.unregisterCallback(mMediaCallback);
                Log.d("MediaController", "MediaController removed");
            }
        }
    }

    /**
     * Notification that the progress level has changed.
     *
     * @param seekBar  The SeekBar whose progress has changed
     * @param progress The current progress level. This will be in the range 0..max where max was
     *                 set by {@link SeekBar#setMax(int)}. (The default value for max is 100.)
     * @param fromUser True if the progress change was initiated by the user.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.media_time_seek_bar:
                if (fromUser && mMediaController != null) {
                    // Seek track to position
                    long duration = mMediaController.getMetadata().getLong(MediaMetadata.METADATA_KEY_DURATION);
                    long position = (long) (progress / 100.0f * (float) duration);
                    mMediaController.getTransportControls().seekTo(position);
                }
                break;
        }
    }

    /**
     * Attaches listeners and gets preferences.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Get preferences
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);

        // Check media access and connect to session
        if (mPrefShowMedia) {
            try {
                mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
                List<MediaController> controllers = mMediaSessionManager.getActiveSessions(new ComponentName(this, NotificationListenerService.class));
                onActiveSessionsChanged(controllers);
                mMediaSessionManager.addOnActiveSessionsChangedListener(this, new ComponentName(this, NotificationListenerService.class));
            } catch (SecurityException e) {
                Log.w("NotificationListener", "No Notification Access");
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_notification_access_title)
                        .setMessage(R.string.dialog_notification_access_message)
                        .setPositiveButton(R.string.dialog_notification_access_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.dialog_notification_access_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mSharedPref.edit().putBoolean("pref_key_show_media", false).putBoolean("pref_key_speak_notifications", false).apply();
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        if (mMediaController == null) {

            Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC);
            PackageManager pm = getPackageManager();
            ResolveInfo info = pm.resolveActivity(i, 0);

            Drawable icon = info.loadIcon(pm);
            mMediaPlay.setPadding(20, 20, 20, 20);
            mMediaPlay.setImageDrawable(icon);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Runnable that updates the position of the seek bar.
     */
    private class UpdatePositionRunnable implements Runnable {
        String mEnd = "-0:00";
        String mPosition = "0:00";
        int mProgress = 0;

        /**
         * Updates the position of the seek bar.
         */
        @Override
        public void run() {
            mMediaTimePosition.setText(mPosition);
            mMediaTimeSeekBar.setProgress(mProgress);
            mMediaTimeEnd.setText(mEnd);
        }

        /**
         * Sets the variables used to display the position.
         *
         * @param position The position of the seek bar.
         * @param progress The text to show to the left of the seek bar
         * @param end      The text to show to the right of the seek bar
         */
        public void setParams(String position, int progress, String end) {
            mPosition = position;
            mProgress = progress;
            mEnd = end;
        }
    }
}