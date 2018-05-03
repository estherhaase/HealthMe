package com.example.android.healthme.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.channels.Pipe;

public class Point_ implements Parcelable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("placeID")
    @Expose
    private String placeID;
    @SerializedName("ref")
    @Expose
    private Ref_ ref;
    public final static Parcelable.Creator<Point_> CREATOR = new Creator<Point_>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Point_ createFromParcel(Parcel in) {
            return new Point_(in);
        }

        public Point_[] newArray(int size) {
            return (new Point_[size]);
        }

    };

    public Point_(){}

    protected Point_(Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.placeID = ((String) in.readValue((String.class.getClassLoader())));
        this.ref = ((Ref_) in.readValue((Ref_.class.getClassLoader())));
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(placeID);
        dest.writeValue(ref);
    }

    public int describeContents() {
        return 0;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public Ref_ getRef() {
        return ref;
    }

    public void setRef(Ref_ ref) {
        this.ref = ref;
    }



}