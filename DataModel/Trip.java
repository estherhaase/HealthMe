package com.example.android.healthme.DataModel;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Trip implements Parcelable
{

    @SerializedName("trip")
    @Expose
    private Trip_ trip;
    public final static Parcelable.Creator<Trip> CREATOR = new Creator<Trip>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        public Trip[] newArray(int size) {
            return (new Trip[size]);
        }

    }
            ;

    protected Trip(Parcel in) {
        this.trip = ((Trip_) in.readValue((Trip_.class.getClassLoader())));
    }

public Trip(){}
    public Trip_ getTrip() {
        return trip;
    }

    public void setTrip(Trip_ trip) {
        this.trip = trip;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(trip);
    }

    public int describeContents() {
        return 0;
    }

}