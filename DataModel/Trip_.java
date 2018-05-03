package com.example.android.healthme.DataModel;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trip_ implements Parcelable
{

    @SerializedName("duration")
    @Expose
    private String duration;
    @SerializedName("interchange")
    @Expose
    private String interchange;
    @SerializedName("legs")
    @Expose
    private List<Leg> legs = null;
    public final static Parcelable.Creator<Trip_> CREATOR = new Creator<Trip_>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Trip_ createFromParcel(Parcel in) {
            return new Trip_(in);
        }

        public Trip_[] newArray(int size) {
            return (new Trip_[size]);
        }

    }
            ;

    protected Trip_(Parcel in) {
        this.duration = ((String) in.readValue((String.class.getClassLoader())));
        this.interchange = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.legs, (Leg.class.getClassLoader()));
    }

public Trip_(){}

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInterchange() {
        return interchange;
    }

    public void setInterchange(String interchange) {
        this.interchange = interchange;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(duration);
        dest.writeValue(interchange);
        dest.writeList(legs);
    }

    public int describeContents() {
        return 0;
    }

}