package org.projects.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nelly on 9/15/16.
 */
public class ProductsInfo implements Parcelable {
    private String name;
    private String number;
    private boolean isCrossed;

    public ProductsInfo(){};

    public ProductsInfo(String name, String number, boolean isCrossed) {
        this.name = name;
        this.number = number;
        this.isCrossed = isCrossed;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){this.name=name;};

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number){this.number=number;};

    public boolean getIsCrossed() {
        return this.isCrossed;
    }

    public void setIsCrossed(boolean crossed) {
        this.isCrossed = crossed;
    }

    public String toString() {
        return this.name + " " + this.number;
    }

    private ProductsInfo(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<ProductsInfo> CREATOR = new Parcelable.Creator<ProductsInfo>() {
        public ProductsInfo createFromParcel(Parcel in) {
            return new ProductsInfo(in);
        }

        public ProductsInfo[] newArray(int size) {

            return new ProductsInfo[size];
        }

    };

    public void readFromParcel(Parcel in) {
        name = in.readString();
        number = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
    }
}
