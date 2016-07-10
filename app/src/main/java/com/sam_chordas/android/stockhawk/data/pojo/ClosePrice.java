package com.sam_chordas.android.stockhawk.data.pojo;

/**
 * Created by Serg on 7/5/2016.
 */
public class ClosePrice implements Comparable<ClosePrice> {

    private float mValue;
    private String mDate;

    public ClosePrice(String value, String date) {
        mValue = Float.parseFloat(value);
        mDate = date;
    }

    public float getValue() {
        return mValue;
    }

    @Override
    public int compareTo(ClosePrice another) {
        return Float.compare(mValue, another.getValue());
    }

    public String getDate() {
        return mDate;
    }
}
