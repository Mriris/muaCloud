
package com.owncloud.android.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.utils.PreferenceUtils;

import java.util.Formatter;
import java.util.Locale;



public class MediaControlView extends FrameLayout implements OnClickListener, OnSeekBarChangeListener {

    private MediaPlayerControl mPlayer;
    private Context mContext;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mDragging;
    private static final int SHOW_PROGRESS = 1;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;

    public MediaControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_control, null);

        mRoot.setFilterTouchesWhenObscured(
                PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        );

        initControllerView(mRoot);
        addView(mRoot, frameParams);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        requestFocus();
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        updatePausePlay();
    }

    private void initControllerView(View v) {
        mPauseButton = v.findViewById(R.id.playBtn);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(this);
        }

        mFfwdButton = v.findViewById(R.id.forwardBtn);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(this);
        }

        mRewButton = v.findViewById(R.id.rewindBtn);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(this);
        }

        mProgress = v.findViewById(R.id.progressBar);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(this);
            }
            mProgress.setMax(1000);
        }

        mEndTime = v.findViewById(R.id.totalTimeText);
        mCurrentTime = v.findViewById(R.id.currentTimeText);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }


    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {




        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {

                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }

        return position;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();

                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    @Override
    public void onClick(View v) {
        int pos;
        boolean playing = mPlayer.isPlaying();
        switch (v.getId()) {

            case R.id.playBtn:
                doPauseResume();
                break;

            case R.id.rewindBtn:
                pos = mPlayer.getCurrentPosition();
                pos -= 5000;
                mPlayer.seekTo(pos);
                if (!playing) {
                    mPlayer.pause();  // necessary in some 2.3.x devices
                }
                setProgress();
                break;

            case R.id.forwardBtn:
                pos = mPlayer.getCurrentPosition();
                pos += 15000;
                mPlayer.seekTo(pos);
                if (!playing) {
                    mPlayer.pause(); // necessary in some 2.3.x devices
                }
                setProgress();
                break;

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {


            return;
        }

        long duration = mPlayer.getDuration();
        long newposition = (duration * progress) / 1000L;
        mPlayer.seekTo((int) newposition);
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime((int) newposition));
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mDragging = true;                           // monitors the duration of dragging 
        mHandler.removeMessages(SHOW_PROGRESS);     // grants no more updates with media player progress while dragging 
    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mDragging = false;
        setProgress();
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);    // grants future updates with media player progress 
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MediaControlView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MediaControlView.class.getName());
    }

}
