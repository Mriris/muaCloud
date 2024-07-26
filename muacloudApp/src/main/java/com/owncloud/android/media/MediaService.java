

package com.owncloud.android.media;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import com.owncloud.android.R;
import com.owncloud.android.presentation.authentication.AccountUtils;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.ui.activity.FileActivity;
import com.owncloud.android.ui.activity.FileDisplayActivity;
import com.owncloud.android.utils.NotificationUtils;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;

import static com.owncloud.android.utils.NotificationConstantsKt.MEDIA_SERVICE_NOTIFICATION_CHANNEL_ID;


public class MediaService extends Service implements OnCompletionListener, OnPreparedListener,
        OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    private static final String MY_PACKAGE = MediaService.class.getPackage() != null ?
            MediaService.class.getPackage().getName() : "com.owncloud.android.media";

    public static final String ACTION_PLAY_FILE = MY_PACKAGE + ".action.PLAY_FILE";
    public static final String ACTION_STOP_ALL = MY_PACKAGE + ".action.STOP_ALL";
    public static final String ACTION_STOP_FILE = MY_PACKAGE + ".action.STOP_FILE";

    public static final String EXTRA_FILE = MY_PACKAGE + ".extra.FILE";
    public static final String EXTRA_ACCOUNT = MY_PACKAGE + ".extra.ACCOUNT";
    public static String EXTRA_START_POSITION = MY_PACKAGE + ".extra.START_POSITION";
    public static final String EXTRA_PLAY_ON_LOAD = MY_PACKAGE + ".extra.PLAY_ON_LOAD";

        public static final int OC_MEDIA_ERROR = 0;

        public static final int MEDIA_CONTROL_SHORT_LIFE = 4000;

        public static final int MEDIA_CONTROL_PERMANENT = 0;

        private static final float DUCK_VOLUME = 0.1f;

        private MediaPlayer mPlayer = null;

        private AudioManager mAudioManager = null;

        private AccountManager mAccountManager;

        enum State {
        STOPPED,
        PREPARING,
        PLAYING,
        PAUSED
    }

        private State mState = State.STOPPED;

        enum AudioFocus {
        NO_FOCUS,
        NO_FOCUS_CAN_DUCK,
        FOCUS
    }

        private AudioFocus mAudioFocus = AudioFocus.NO_FOCUS;

        private boolean mIsStreaming = false;

        private WifiLock mWifiLock;

    private static final String MEDIA_WIFI_LOCK_TAG = MY_PACKAGE + ".WIFI_LOCK";

        private NotificationManager mNotificationManager;

        private OCFile mFile;

        private MediaFileObserver mFileObserver = null;

        private Account mAccount;

        protected boolean mPlayOnPrepared;

        private int mStartPosition;

        private IBinder mBinder;

        private MediaControlView mMediaController;

        private NotificationCompat.Builder mNotificationBuilder;


    public static String getMessageForMediaError(Context context, int what, int extra) {
        int messageId;

        if (what == OC_MEDIA_ERROR) {
            messageId = extra;

        } else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
                        messageId = R.string.media_err_unsupported;

        } else if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                        messageId = R.string.media_err_io;

        } else if (extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
                        messageId = R.string.media_err_malformed;

        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                        messageId = R.string.media_err_timeout;

        } else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                        messageId = R.string.media_err_invalid_progressive_playback;

        } else {
                                    messageId = R.string.media_err_unknown;
        }
        return context.getString(messageId);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("Creating ownCloud media service");

        mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).
                createWifiLock(WifiManager.WIFI_MODE_FULL, MEDIA_WIFI_LOCK_TAG);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setColor(this.getResources().getColor(R.color.primary));
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mBinder = new MediaServiceBinder(this);

        mAccountManager = AccountManager.get(this);
        mAccountManager.addOnAccountsUpdatedListener(new OnAccountsUpdateListener() {
            @Override
            public void onAccountsUpdated(Account[] accounts) {

                if (mAccount != null && !AccountUtils.exists(mAccount.name, MediaService.this)) {
                    processStopRequest(false);
                }
            }
        }, null, false);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(ACTION_PLAY_FILE)) {
            processPlayFileRequest(intent);

        } else if (action.equals(ACTION_STOP_ALL)) {
            processStopRequest(true);
        } else if (action.equals(ACTION_STOP_FILE)) {
            processStopFileRequest(intent);
        }

        return START_NOT_STICKY; // don't want it to restart in case it's killed.
    }

    private void processStopFileRequest(Intent intent) {
        OCFile file = intent.getExtras().getParcelable(EXTRA_FILE);
        if (file != null && file.equals(mFile)) {
            processStopRequest(true);
        }
    }


    private void processPlayFileRequest(Intent intent) {
        if (mState != State.PREPARING) {
            mFile = intent.getExtras().getParcelable(EXTRA_FILE);
            mAccount = intent.getExtras().getParcelable(EXTRA_ACCOUNT);
            mPlayOnPrepared = intent.getExtras().getBoolean(EXTRA_PLAY_ON_LOAD, false);
            mStartPosition = intent.getExtras().getInt(EXTRA_START_POSITION, 0);
            tryToGetAudioFocus();
            playMedia();
        }
    }


    protected void processPlayRequest() {

        tryToGetAudioFocus();

        if (mState == State.STOPPED) {

            playMedia();

        } else if (mState == State.PAUSED) {

            mState = State.PLAYING;
            setUpAsForeground(String.format(getString(R.string.media_state_playing), mFile.getFileName()));
            configAndStartMediaPlayer();
        }
    }


    protected void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);

        } else {
            mPlayer.reset();
        }
    }


    protected void processPauseRequest() {
        if (mState == State.PLAYING) {
            mState = State.PAUSED;
            mPlayer.pause();
            releaseResources(false); // retain media player in pause

        }
    }


    protected void processStopRequest(boolean force) {
        if (mState != State.PREPARING || force) {
            mState = State.STOPPED;
            mFile = null;
            stopFileObserver();
            mAccount = null;
            releaseResources(true);
            giveUpAudioFocus();
            stopSelf();     // service is no longer necessary
        }
    }


    protected void releaseResources(boolean releaseMediaPlayer) {

        stopForeground(true);

        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }


    private void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.FOCUS
                && mAudioManager != null
                && AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this)) {

            mAudioFocus = AudioFocus.NO_FOCUS;
        }
    }


    protected void configAndStartMediaPlayer() {
        if (mPlayer == null) {
            throw new IllegalStateException("mPlayer is NULL");
        }

        if (mAudioFocus == AudioFocus.NO_FOCUS) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();        // have to be polite; but mState is not changed, to resume when focus is

            }

        } else {
            if (mAudioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
                mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);

            } else {
                mPlayer.setVolume(1.0f, 1.0f); // full volume
            }

            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
        }
    }


    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.FOCUS
                && mAudioManager != null
                && (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN))
        ) {
            mAudioFocus = AudioFocus.FOCUS;
        }
    }

    
    protected void playMedia() {
        mState = State.STOPPED;
        releaseResources(false); // release everything except MediaPlayer

        try {
            if (mFile == null) {
                Toast.makeText(this, R.string.media_err_nothing_to_play, Toast.LENGTH_LONG).show();
                processStopRequest(true);
                return;

            } else if (mAccount == null) {
                Toast.makeText(this, R.string.media_err_not_in_owncloud, Toast.LENGTH_LONG).show();
                processStopRequest(true);
                return;
            }

            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String url = mFile.getStoragePath();
            updateFileObserver(url);
                        mIsStreaming = false;

            mPlayer.setDataSource(url);

            mState = State.PREPARING;
            setUpAsForeground(String.format(getString(R.string.media_state_loading), mFile.getFileName()));

            mPlayer.prepareAsync();

            if (mIsStreaming) {
                mWifiLock.acquire();
            } else if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }

        } catch (SecurityException e) {
            Timber.e(e, "SecurityException playing " + mAccount.name + mFile.getRemotePath());
            Toast.makeText(this, String.format(getString(R.string.media_err_security_ex), mFile.getFileName()),
                    Toast.LENGTH_LONG).show();
            processStopRequest(true);

        } catch (IOException e) {
            Timber.e(e, "IOException playing " + mAccount.name + mFile.getRemotePath());
            Toast.makeText(this, String.format(getString(R.string.media_err_io_ex), mFile.getFileName()),
                    Toast.LENGTH_LONG).show();
            processStopRequest(true);

        } catch (IllegalStateException e) {
            Timber.e(e, "IllegalStateException " + mAccount.name + mFile.getRemotePath());
            Toast.makeText(this, String.format(getString(R.string.media_err_unexpected), mFile.getFileName()),
                    Toast.LENGTH_LONG).show();
            processStopRequest(true);

        } catch (IllegalArgumentException e) {
            Timber.e(e, "IllegalArgumentException " + mAccount.name + mFile.getRemotePath());
            Toast.makeText(this, String.format(getString(R.string.media_err_unexpected), mFile.getFileName()),
                    Toast.LENGTH_LONG).show();
            processStopRequest(true);
        }
    }

    private void updateFileObserver(String url) {
        stopFileObserver();
        mFileObserver = new MediaFileObserver(url);
        mFileObserver.startWatching();
    }

    private void stopFileObserver() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

        public void onCompletion(MediaPlayer player) {
        Toast.makeText(this, String.format(getString(R.string.media_event_done), mFile.getFileName()),
                Toast.LENGTH_LONG).show();
        if (mMediaController != null) {

            player.seekTo(0);
            processPauseRequest();
            mMediaController.updatePausePlay();
        } else {

            processStopRequest(true);
        }
    }

    
    public void onPrepared(MediaPlayer player) {
        mState = State.PLAYING;
        updateNotification(String.format(getString(R.string.media_state_playing), mFile.getFileName()));
        if (mMediaController != null) {
            mMediaController.setEnabled(true);
        }
        player.seekTo(mStartPosition);
        configAndStartMediaPlayer();
        if (!mPlayOnPrepared) {
            processPauseRequest();
        }

        if (mMediaController != null) {
            mMediaController.updatePausePlay();
        }
    }

    
    private void updateNotification(String content) {
        String ticker = String.format(getString(R.string.media_notif_ticker), getString(R.string.app_name));

        Intent showDetailsIntent = new Intent(this, FileDisplayActivity.class);
        showDetailsIntent.putExtra(FileActivity.EXTRA_FILE, mFile);
        showDetailsIntent.putExtra(FileActivity.EXTRA_ACCOUNT, mAccount);
        showDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                (int) System.currentTimeMillis(),
                showDetailsIntent,
                NotificationUtils.pendingIntentFlags));
        mNotificationBuilder.setWhen(System.currentTimeMillis());
        mNotificationBuilder.setTicker(ticker);
        mNotificationBuilder.setContentTitle(ticker);
        mNotificationBuilder.setContentText(content);
        mNotificationBuilder.setChannelId(MEDIA_SERVICE_NOTIFICATION_CHANNEL_ID);

        mNotificationManager.notify(R.string.media_notif_ticker, mNotificationBuilder.build());
    }

    
    private void setUpAsForeground(String content) {
        String ticker = String.format(getString(R.string.media_notif_ticker), getString(R.string.app_name));


        mNotificationBuilder.setSmallIcon(R.drawable.ic_play_arrow);

        mNotificationBuilder.setWhen(System.currentTimeMillis());
        mNotificationBuilder.setOngoing(true);

        Intent showDetailsIntent = new Intent(this, FileDisplayActivity.class);
        showDetailsIntent.putExtra(FileActivity.EXTRA_FILE, mFile);
        showDetailsIntent.putExtra(FileActivity.EXTRA_ACCOUNT, mAccount);
        showDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                (int) System.currentTimeMillis(),
                showDetailsIntent,
                NotificationUtils.pendingIntentFlags));
        mNotificationBuilder.setContentTitle(ticker);
        mNotificationBuilder.setContentText(content);
        mNotificationBuilder.setChannelId(MEDIA_SERVICE_NOTIFICATION_CHANNEL_ID);

        startForeground(R.string.media_notif_ticker, mNotificationBuilder.build());
    }

    
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Timber.e("Error in audio playback, what = " + what + ", extra = " + extra);

        String message = getMessageForMediaError(this, what, extra);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        processStopRequest(true);
        return true;
    }

    
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange > 0) {

            mAudioFocus = AudioFocus.FOCUS;

            if (mState == State.PLAYING) {
                configAndStartMediaPlayer();
            }

        } else if (focusChange < 0) {

            boolean canDuck = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK == focusChange;
            mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS;

            if (mPlayer != null && mPlayer.isPlaying()) {
                configAndStartMediaPlayer();
            }
        }

    }

    
    @Override
    public void onDestroy() {
        mState = State.STOPPED;
        releaseResources(true);
        giveUpAudioFocus();
        stopForeground(true);
        super.onDestroy();
    }

    
    @Override
    public IBinder onBind(Intent arg) {
        return mBinder;
    }

    
    @Override
    public boolean onUnbind(Intent intent) {
        if (mState == State.PAUSED || mState == State.STOPPED) {
            processStopRequest(false);
        }
        return false;   // not accepting rebinding (default behaviour)
    }

    
    protected MediaPlayer getPlayer() {
        return mPlayer;
    }

    
    protected OCFile getCurrentFile() {
        return mFile;
    }

    
    protected State getState() {
        return mState;
    }

    protected void setMediaController(MediaControlView mediaController) {
        mMediaController = mediaController;
    }

    protected MediaControlView getMediaController() {
        return mMediaController;
    }

    
    private class MediaFileObserver extends FileObserver {

        public MediaFileObserver(String path) {
            super((new File(path)).getParent(), FileObserver.DELETE | FileObserver.MOVED_FROM);
        }

        @Override
        public void onEvent(int event, String path) {
            if (path != null && path.equals(mFile.getFileName())) {
                Timber.d("Media file deleted or moved out of sight, stopping playback");
                processStopRequest(true);
            }
        }
    }

}
