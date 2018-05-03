package com.example.android.healthme.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data implements Parcelable
{

    @SerializedName("trips")
    @Expose
    private List<Trip> trips = null;
    public final static Parcelable.Creator<Data> CREATOR = new Creator<Data>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        public Data[] newArray(int size) {
            return (new Data[size]);
        }

    }
            ;

    protected Data(Parcel in) {
        in.readList(this.trips, (Trip.class.getClassLoader()));
    }

    public Data() {}

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(trips);
    }

    public int describeContents() {
        return 0;
    }

}