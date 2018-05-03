package com.example.android.healthme.DataModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ref_ implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("platform")
    @Expose
    private String platform;
    public final static Parcelable.Creator<Ref_> CREATOR = new Creator<Ref_>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Ref_ createFromParcel(Parcel in) {
            return new Ref_(in);
        }

        public Ref_[] newArray(int size) {
            return (new Ref_[size]);
        }

    }
            ;

    protected Ref_(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.platform = ((String) in.readValue((String.class.getClassLoader())));
    }

public Ref_(){}
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