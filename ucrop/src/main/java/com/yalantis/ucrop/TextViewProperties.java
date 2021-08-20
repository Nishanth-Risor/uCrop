package com.yalantis.ucrop;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kotlin.jvm.internal.Intrinsics;

public class TextViewProperties extends ArrayList implements Parcelable {
    private float x;
    private float y;
    private float color;
    private float rotation;
    private float scaleX;
    private float scaleY;

    protected TextViewProperties(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
        color = in.readFloat();
        rotation = in.readFloat();
        scaleX = in.readFloat();
        scaleY = in.readFloat();
    }

    public final static Creator<TextViewProperties> CREATOR = new Creator<TextViewProperties>() {
        @Override
        public TextViewProperties createFromParcel(Parcel in) {
            return new TextViewProperties(in);
        }

        @Override
        public TextViewProperties[] newArray(int size) {
            return new TextViewProperties[size];
        }
    };

    public void writeToParcel(@NotNull Parcel parcel, int flags) {
        Intrinsics.checkNotNullParameter(parcel, "parcel");
        parcel.writeFloat(this.x);
        parcel.writeFloat(this.y);
        parcel.writeFloat(this.color);
        parcel.writeFloat(this.rotation);
        parcel.writeFloat(this.scaleX);
        parcel.writeFloat(this.scaleY);
    }

    public int describeContents() {
        return 0;
    }

    public final float getX() {
        return this.x;
    }

    public final void setX(float var1) {
        this.x = var1;
    }

    public final float getY() {
        return this.y;
    }

    public final void setY(float var1) {
        this.y = var1;
    }

    public final float getColor() {
        return this.color;
    }

    public final void setColor(float var1) {
        this.color = var1;
    }

    public final float getRotation() {
        return this.rotation;
    }

    public final void setRotation(float var1) {
        this.rotation = var1;
    }

    public final float getScaleX() {
        return this.scaleX;
    }

    public final void setScaleX(float var1) {
        this.scaleX = var1;
    }

    public final float getScaleY() {
        return this.scaleY;
    }

    public final void setScaleY(float var1) {
        this.scaleY = var1;
    }

    public TextViewProperties(float x, float y, float color, float rotation, float scaleX, float scaleY) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
    @NotNull
    public final TextViewProperties copy(float x, float y, float color, float rotation, float scaleX, float scaleY) {
        return new TextViewProperties(x, y, color, rotation, scaleX, scaleY);
    }

    @NotNull
    public String toString() {
        return "TextViewProperties(x=" + this.x + ", y=" + this.y + ", color=" + this.color + ", rotation=" + this.rotation + ", scaleX=" + this.scaleX + ", scaleY=" + this.scaleY + ")";
    }

    public int hashCode() {
        return ((((Float.floatToIntBits(this.x) * 31 + Float.floatToIntBits(this.y)) * 31 + Float.floatToIntBits(this.color)) * 31 + Float.floatToIntBits(this.rotation)) * 31 + Float.floatToIntBits(this.scaleX)) * 31 + Float.floatToIntBits(this.scaleY);
    }

    public boolean equals(@org.jetbrains.annotations.Nullable Object var1) {
        if (this != var1) {
            if (var1 instanceof TextViewProperties) {
                TextViewProperties var2 = (TextViewProperties)var1;
                if (Float.compare(this.x, var2.x) == 0 && Float.compare(this.y, var2.y) == 0 && Float.compare(this.color, var2.color) == 0 && Float.compare(this.rotation, var2.rotation) == 0 && Float.compare(this.scaleX, var2.scaleX) == 0 && Float.compare(this.scaleY, var2.scaleY) == 0) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
}
