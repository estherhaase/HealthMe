package com.example.android.healthme.DataModel;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ref implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("platform")
    @Expose
    private String platform;
    public final static Parcelable.Creator<Ref> CREATOR = new Creator<Ref>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Ref createFromParcel(Parcel in) {
            return new Ref(in);
        }

        public Ref[] newArray(int size) {
            return (new Ref[size]);
        }

    }
            ;

    protected Ref(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.platform = ((String) in.readValue((String.class.getClassLoader())));
    }
public Ref(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(platform);
    }

    public int describeContents() {
        return 0;
    }

}