package com.yalantis.ucrop

import android.os.Parcel
import android.os.Parcelable

data class TextViewProperties(var x:Float=-1f, var y:Float=-1f,  var color:Float,var rotation:Float,  var scaleX:Float=1f, var scaleY:Float=1f): Parcelable, ArrayList<Float>() {
    constructor(parcel: Parcel) : this(
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(color)
        parcel.writeFloat(rotation)
        parcel.writeFloat(scaleX)
        parcel.writeFloat(scaleY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TextViewProperties> {
        override fun createFromParcel(parcel: Parcel): TextViewProperties {
            return TextViewProperties(parcel)
        }

        override fun newArray(size: Int): Array<TextViewProperties?> {
            return arrayOfNulls(size)
        }
    }

}