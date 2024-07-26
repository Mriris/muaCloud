

package com.owncloud.android.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import com.owncloud.android.utils.BitmapUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public class DefaultAvatarTextDrawable extends Drawable {

    private final String mText;


    private final Paint mTextPaint;


    private final Paint mBackground;


    private final float mRadius;


    public DefaultAvatarTextDrawable(String text, int r, int g, int b, float radius) {
        mRadius = radius;
        mText = text;

        mBackground = new Paint();
        mBackground.setStyle(Paint.Style.FILL);
        mBackground.setAntiAlias(true);
        mBackground.setColor(Color.rgb(r, g, b));

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(radius);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }


    @NonNull
    public static DefaultAvatarTextDrawable createAvatar(String accountName, float radiusInDp) throws
            UnsupportedEncodingException, NoSuchAlgorithmException {
        int[] rgb = BitmapUtils.calculateAvatarBackgroundRGB(accountName);
        DefaultAvatarTextDrawable avatar = new DefaultAvatarTextDrawable(
                accountName.substring(0, 1).toUpperCase(), rgb[0], rgb[1], rgb[2], radiusInDp);
        return avatar;
    }


    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(mRadius, mRadius, mRadius, mBackground);
        canvas.drawText(mText, mRadius, mRadius - ((mTextPaint.descent() + mTextPaint.ascent()) / 2), mTextPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mTextPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
