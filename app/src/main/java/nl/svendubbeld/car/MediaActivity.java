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

package nl.svendubbeld.car;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
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

public class MediaActivity extends Activity
        implements MediaSessionManager.OnActiveSessionsChangedListener, SeekBar.OnSeekBarChangeListener {
    Log mLog = new Log();
    TextView mMediaAlbum;
    ImageView mMediaArt;
    TextView mMediaArtist;
    CardView mMediaContainer;
    MediaController mMediaController = null;
    ImageView mMediaNext;
    ImageView mMediaPlay;
    ProgressBar mMediaPlayProgress;
    ImageView mMediaPrev;
    MediaSessionManager mMediaSessionManager;
    TextView mMediaTimeEnd;
    TextView mMediaTimePosition;
    SeekBar mMediaTimeSeekBar;
    TextView mMediaTitle;
    TextView mMediaUpNextArtist;
    TextView mMediaUpNextTitle;
    ImageView mMediaVolDown;
    ImageView mMediaVolUp;
    boolean mPrefShowMedia = true;
    SharedPreferences mSharedPref;
    Timer mTimer;
    TimerTask mUpdatePositionTask;

    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            mLog.d("MediaController", "MediaController removed");
            mMediaController = null;
        }
        if (controllers.size() > 0) {
            mMediaController = controllers.get(0);
            mMediaController.registerCallback(mMediaCallback);
            mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
            mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
            mLog.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media);

        // Get views
        mMediaContainer = ((CardView) findViewById(R.id.media_container));
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

        mTimer = new Timer();
        final UpdatePositionRunnable updatePositionRunnable = new UpdatePositionRunnable();
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
                        updatePositionRunnable.setParams(position, progress, end);
                        runOnUiThread(updatePositionRunnable);
                    }
                }
            }
        };

        mTimer.scheduleAtFixedRate(mUpdatePositionTask, 100l, 1000l);
    }

    protected void onPause() {
        super.onPause();
        if (mPrefShowMedia) {
            mMediaSessionManager.removeOnActiveSessionsChangedListener(this);
            if (mMediaController != null) {
                mMediaController.unregisterCallback(mMediaCallback);
                mLog.d("MediaController", "MediaController removed");
            }
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.media_time_seek_bar:
                if (fromUser && mMediaController != null) {
                    long duration = mMediaController.getMetadata().getLong(MediaMetadata.METADATA_KEY_DURATION);
                    long position = (long) (progress / 100.0f * (float) duration);
                    mMediaController.getTransportControls().seekTo(position);
                }
                break;
        }
    }

    protected void onResume() {
        super.onResume();

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefShowMedia = mSharedPref.getBoolean("pref_key_show_media", true);

        if (mPrefShowMedia) {
            try {
                mMediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
                List<MediaController> controllers = mMediaSessionManager.getActiveSessions(new ComponentName(this, NotificationListener.class));
                if (controllers.size() > 0) {
                    mMediaController = controllers.get(0);
                    mMediaController.registerCallback(mMediaCallback);
                    mMediaCallback.onMetadataChanged(mMediaController.getMetadata());
                    mMediaCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                    mLog.d("MediaController", "MediaController set: " + mMediaController.getPackageName());
                }
                mMediaSessionManager.addOnActiveSessionsChangedListener(this, new ComponentName(this, NotificationListener.class));
            } catch (SecurityException localSecurityException) {
                mLog.w("NotificationListener", "No Notification Access");
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
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    class UpdatePositionRunnable implements Runnable {
        String mEnd = "-0:00";
        String mPosition = "0:00";
        int mProgress = 0;

        public void run() {
            mMediaTimePosition.setText(mPosition);
            mMediaTimeSeekBar.setProgress(mProgress);
            mMediaTimeEnd.setText(mEnd);
        }

        void setParams(String position, int progress, String end) {
            mPosition = position;
            mProgress = progress;
            mEnd = end;
        }
    }

    MediaController.Callback mMediaCallback = new MediaController.Callback() {
        public void onAudioInfoChanged(MediaController.PlaybackInfo playbackInfo) {
            super.onAudioInfoChanged(playbackInfo);
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);

            if ((mMediaArt != null) && (metadata != null)) {
                mMediaArt.setImageBitmap(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART));
                mMediaTitle.setText(metadata.getText(MediaMetadata.METADATA_KEY_TITLE));
                mMediaArtist.setText(metadata.getText(MediaMetadata.METADATA_KEY_ARTIST));
                mMediaAlbum.setText(metadata.getText(MediaMetadata.METADATA_KEY_ALBUM));

                PlaybackState state = mMediaController.getPlaybackState();
                if (state != null) {
                    long queueId = state.getActiveQueueItemId();

                    if (queueId != MediaSession.QueueItem.UNKNOWN_ID) {
                        List<MediaSession.QueueItem> queue = mMediaController.getQueue();

                        int i;
                        for (i = 0; queue.get(i).getQueueId() != queueId; i++) ;
                        MediaSession.QueueItem nextItem = queue.get(i + 1);

                        mMediaUpNextTitle.setText(nextItem.getDescription().getTitle());
                        mMediaUpNextArtist.setText(nextItem.getDescription().getSubtitle());
                    }

                    mUpdatePositionTask.run();
                }
            }
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            if ((mMediaPlay != null) && (state != null)) {
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

                long queueId = state.getActiveQueueItemId();

                if (queueId != MediaSession.QueueItem.UNKNOWN_ID) {
                    List<MediaSession.QueueItem> queue = mMediaController.getQueue();

                    int i;
                    for (i = 0; queue.get(i).getQueueId() != queueId; i++) ;
                    MediaSession.QueueItem nextItem = queue.get(i + 1);

                    mMediaUpNextTitle.setText(nextItem.getDescription().getTitle());
                    mMediaUpNextArtist.setText(nextItem.getDescription().getSubtitle());
                }

                mUpdatePositionTask.run();
            }
        }

        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);

            PlaybackState state = mMediaController.getPlaybackState();
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

        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }
    };

    View.OnClickListener mMediaControlsListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mMediaController != null) {
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
                Intent mediaIntent = new Intent("android.intent.action.MEDIA_BUTTON");
                mediaIntent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
                sendOrderedBroadcast(mediaIntent, null);

                mediaIntent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
                sendOrderedBroadcast(mediaIntent, null);

                mMediaPlay.setVisibility(View.GONE);
                mMediaPlayProgress.setVisibility(View.VISIBLE);
            }
        }
    };
}