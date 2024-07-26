

package com.owncloud.android.ui.helpers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;


public class SparseBooleanArrayParcelable implements Parcelable {

    public static Parcelable.Creator<SparseBooleanArrayParcelable> CREATOR =
            new Parcelable.Creator<SparseBooleanArrayParcelable>() {

                @Override
                public SparseBooleanArrayParcelable createFromParcel(Parcel source) {

                    int size = source.readInt();

                    SparseBooleanArray sba = new SparseBooleanArray();
                    int key;
                    boolean value;
                    for (int i = 0; i < size; i++) {
                        key = source.readInt();
                        value = (source.readInt() != 0);
                        sba.put(key, value);
                    }

                    return new SparseBooleanArrayParcelable(sba);
                }

                @Override
                public SparseBooleanArrayParcelable[] newArray(int size) {
                    return new SparseBooleanArrayParcelable[size];
                }
            };

    private final SparseBooleanArray mSba;

    public SparseBooleanArrayParcelable(SparseBooleanArray sba) {
        if (sba == null) {
            throw new IllegalArgumentException("Cannot wrap a null SparseBooleanArray");
        }
        mSba = sba;
    }

    public SparseBooleanArray getSparseBooleanArray() {
        return mSba;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mSba.size());

        for (int i = 0; i < mSba.size(); i++) {
            dest.writeInt(mSba.keyAt(i));
            dest.writeInt(mSba.valueAt(i) ? 1 : 0);
        }

    }
}