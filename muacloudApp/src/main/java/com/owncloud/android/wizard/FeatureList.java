

package com.owncloud.android.wizard;

import android.os.Parcel;
import android.os.Parcelable;

import com.owncloud.android.R;


public class FeatureList {

    static final private FeatureItem[] featuresList = {

            new FeatureItem(R.drawable.whats_new_files, R.string.welcome_feature_1_title,
                    R.string.welcome_feature_1_text),
            new FeatureItem(R.drawable.whats_new_share, R.string.welcome_feature_2_title,
                    R.string.welcome_feature_2_text),
            new FeatureItem(R.drawable.whats_new_accounts, R.string.welcome_feature_3_title,
                    R.string.welcome_feature_3_text),
            new FeatureItem(R.drawable.whats_new_camera_uploads, R.string.welcome_feature_4_title,
                    R.string.welcome_feature_4_text),
            new FeatureItem(R.drawable.whats_new_video_streaming, R.string.welcome_feature_5_title,
                    R.string.welcome_feature_5_text)
    };

    static public FeatureItem[] get() {
        return featuresList;
    }

    static public class FeatureItem implements Parcelable {
        private static final int DO_NOT_SHOW = -1;
        private final int image;
        private final int titleText;
        private final int contentText;

        private FeatureItem(int image, int titleText, int contentText) {
            this.image = image;
            this.titleText = titleText;
            this.contentText = contentText;
        }

        public boolean shouldShowImage() {
            return image != DO_NOT_SHOW;
        }

        public int getImage() {
            return image;
        }

        public boolean shouldShowTitleText() {
            return titleText != DO_NOT_SHOW;
        }

        public int getTitleText() {
            return titleText;
        }

        public boolean shouldShowContentText() {
            return contentText != DO_NOT_SHOW;
        }

        public int getContentText() {
            return contentText;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(image);
            dest.writeInt(titleText);
            dest.writeInt(contentText);
        }

        private FeatureItem(Parcel p) {
            image = p.readInt();
            titleText = p.readInt();
            contentText = p.readInt();
        }

        public static final Parcelable.Creator CREATOR =
                new Parcelable.Creator() {

                    @Override
                    public Object createFromParcel(Parcel source) {
                        return new FeatureItem(source);
                    }

                    @Override
                    public Object[] newArray(int size) {
                        return new FeatureItem[size];
                    }
                };
    }
}
