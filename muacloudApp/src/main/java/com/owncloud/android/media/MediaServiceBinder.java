

package com.owncloud.android.media;

import android.accounts.Account;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.widget.MediaController;

import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.media.MediaService.State;
import timber.log.Timber;


public class MediaServiceBinder extends Binder implements MediaController.MediaPlayerControl {


    private MediaService mService = null;


    public MediaServiceBinder(MediaService service) {
        if (service == null) {
            throw new IllegalArgumentException("Argument 'service' can not be null");
        }
        mService = service;
    }

    public boolean isPlaying(OCFile mFile) {
        return (mFile != null && mFile.equals(mService.getCurrentFile()));
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        MediaPlayer currentPlayer = mService.getPlayer();
        if (currentPlayer != null) {
            return 100;

        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        MediaPlayer currentPlayer = mService.getPlayer();
        if (currentPlayer != null) {
            return currentPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public int getDuration() {
        MediaPlayer currentPlayer = mService.getPlayer();
        if (currentPlayer != null) {
            return currentPlayer.getDuration();
        } else {
            return 0;
        }
    }


    @Override
    public boolean isPlaying() {
        MediaService.State currentState = mService.getState();
        return (currentState == State.PLAYING || (currentState == State.PREPARING && mService.mPlayOnPrepared));
    }

    @Override
    public void pause() {
        Timber.d("Pausing through binder...");
        mService.processPauseRequest();
    }

    @Override
    public void seekTo(int pos) {
        Timber.d("Seeking " + pos + " through binder...");
        MediaPlayer currentPlayer = mService.getPlayer();
        MediaService.State currentState = mService.getState();
        if (currentPlayer != null && currentState != State.PREPARING && currentState != State.STOPPED) {
            currentPlayer.seekTo(pos);
        }
    }

    @Override
    public void start() {
        Timber.d("Starting through binder...");
        mService.processPlayRequest();  // this will finish the service if there is no file preloaded to play
    }

    public void start(Account account, OCFile file, boolean playImmediately, int position) {
        Timber.d("Loading and starting through binder...");
        Intent i = new Intent(mService, MediaService.class);
        i.putExtra(MediaService.EXTRA_ACCOUNT, account);
        i.putExtra(MediaService.EXTRA_FILE, file);
        i.putExtra(MediaService.EXTRA_PLAY_ON_LOAD, playImmediately);
        i.putExtra(MediaService.EXTRA_START_POSITION, position);
        i.setAction(MediaService.ACTION_PLAY_FILE);
        mService.startService(i);
    }

    public void registerMediaController(MediaControlView mediaController) {
        mService.setMediaController(mediaController);
    }

    public void unregisterMediaController(MediaControlView mediaController) {
        if (mediaController != null && mediaController == mService.getMediaController()) {
            mService.setMediaController(null);
        }

    }

    public boolean isInPlaybackState() {
        MediaService.State currentState = mService.getState();
        return (currentState == MediaService.State.PLAYING || currentState == MediaService.State.PAUSED);
    }

    @Override
    public int getAudioSessionId() {
        return 1; // not really used
    }

}


