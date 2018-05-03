package com.example.android.healthme.DataModel;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Leg implements Parcelable
{

    @SerializedName("timeMinute")
    @Expose
    private String timeMinute;
    @SerializedName("points")
    @Expose
    private List<Point> points = null;
    @SerializedName("stopSeq")
    @Expose
    private List<StopSeq> stopSeq = null;
    @SerializedName("mode")
    @Expose
    private Mode mode;
    public final static Parcelable.Creator<Leg> CREATOR = new Creator<Leg>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Leg createFromParcel(Parcel in) {
            return new Leg(in);
        }

        public Leg[] newArray(int size) {
            return (new Leg[size]);
        }

    };
    public Leg(){}

    protected Leg(Parcel in) {
        this.timeMinute = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.points, (Point.class.getClassLoader()));
        in.readList(this.stopSeq, (StopSeq.class.getClassLoader()));
    }



    public String getTimeMinute() {
        return timeMinute;
    }

    public void setTimeMinute(String timeMinute) {
        this.timeMinute = timeMinute;
    }

    public List<Point> getPoints() {
        return points;
    }

    public Mode getMode() {
        return mode;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<StopSeq> getStopSeq() {
        return stopSeq;
    }

    public void setStopSeq(List<StopSeq> stopSeq) {
        this.stopSeq = stopSeq;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(timeMinute);
        dest.writeList(points);
        dest.writeList(stopSeq);
    }

    public int describeContents() {
        return 0;
    }

}
