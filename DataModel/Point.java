package com.example.android.healthme.DataModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Point implements Parcelable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("placeID")
    @Expose
    private String placeID;
    @SerializedName("ref")
    @Expose
    private Ref ref;
    public final static Parcelable.Creator<Point> CREATOR = new Creator<Point>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        public Point[] newArray(int size) {
            return (new Point[size]);
        }

    };

    public Point(){}

    protected Point(Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.placeID = ((String) in.readValue((String.class.getClassLoader())));
        this.ref = ((Ref) in.readValue((Ref.class.getClassLoader())));
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

    public Ref getRef() {
        return ref;
    }

    public void setRef(Ref ref) {
        this.ref = ref;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(placeID);
        dest.writeValue(ref);
    }

    public int describeContents() {
        return 0;
    }

}