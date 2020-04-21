package com.example.starwarsplanetmanager
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import org.json.*
import java.io.Serializable


class _Governor() : Parcelable {
    var IDPlanet: Int = 0
    var Name: String = ""
    var Surname: String = ""
    var Image: String? = null

    constructor(parcel: Parcel) : this() {
        IDPlanet = parcel.readInt()
        Name = parcel.readString().toString()
        Surname = parcel.readString().toString()
        Image = parcel.readString().toString()
    }

    constructor(rawData: JSONObject):this(){
        this.IDPlanet = rawData.getInt("IDPlanet")
        this.Name = rawData.getString("Name")
        this.Surname = rawData.getString("Surname")
        this.Image = null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(IDPlanet)
        parcel.writeString(Name)
        parcel.writeString(Surname)
        parcel.writeString(Image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<_Governor> {
        override fun createFromParcel(parcel: Parcel): _Governor {
            return _Governor(parcel)
        }

        override fun newArray(size: Int): Array<_Governor?> {
            return arrayOfNulls(size)
        }
    }

}

class _ArmyType() : Parcelable {
    var IDPlanet: Int = 0
    var B1: Int = 0
    var B2: Int = 0
    var B3: Int = 0
    var BX: Int = 0
    var BL: Int = 0
    var D1S1: Int = 0
    var HKB3: Int = 0
    var IG110: Int = 0
    var OG9: Int = 0
    var T4: Int = 0

    constructor(parcel: Parcel) : this() {
        IDPlanet = parcel.readInt()
        B1 = parcel.readInt()
        B2 = parcel.readInt()
        B3 = parcel.readInt()
        BX = parcel.readInt()
        BL = parcel.readInt()
        D1S1 = parcel.readInt()
        HKB3 = parcel.readInt()
        IG110 = parcel.readInt()
        OG9 = parcel.readInt()
        T4 = parcel.readInt()
    }

    constructor(rawData:JSONObject):this(){
        this.IDPlanet = rawData.getInt("IDPlanet")
        this.B1 = rawData.getInt("B1")
        this.B2 = rawData.getInt("B2")
        this.B3 = rawData.getInt("B3")
        this.BX = rawData.getInt("BX")
        this.BL = rawData.getInt("BL")
        this.D1S1 = rawData.getInt("D1S1")
        this.HKB3 = rawData.getInt("HKB3")
        this.OG9 = rawData.getInt("OG9")
        this.IG110 = rawData.getInt("IG110")
        this.T4 = rawData.getInt("T4")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(IDPlanet)
        parcel.writeInt(B1)
        parcel.writeInt(B2)
        parcel.writeInt(B3)
        parcel.writeInt(BX)
        parcel.writeInt(BL)
        parcel.writeInt(D1S1)
        parcel.writeInt(HKB3)
        parcel.writeInt(IG110)
        parcel.writeInt(OG9)
        parcel.writeInt(T4)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<_ArmyType> {
        override fun createFromParcel(parcel: Parcel): _ArmyType {
            return _ArmyType(parcel)
        }

        override fun newArray(size: Int): Array<_ArmyType?> {
            return arrayOfNulls(size)
        }
    }
}


class Planet() : Parcelable {
    var IDPlanet: Int = 0
    var Name: String = ""
    var Type: String = ""
    var Government:String = ""
    var RegionID: Int = 0
    var Image: String? = null
    var RegionName: String = ""
    var Governor: _Governor? = null
    var ArmyType: _ArmyType? = null

    constructor(parcel: Parcel) : this() {
        IDPlanet = parcel.readInt()
        Name = parcel.readString().toString()
        Type = parcel.readString().toString()
        Government = parcel.readString().toString()
        RegionID = parcel.readInt()
        Image = parcel.readString().toString()
        RegionName = parcel.readString().toString()
        Governor = parcel.readParcelable(_Governor::class.java.classLoader)
        ArmyType = parcel.readParcelable(_ArmyType::class.java.classLoader)
    }

    constructor(rawData:JSONObject) : this() {
        this.IDPlanet = rawData.getInt("IDPlanet")
        this.Name = rawData.getString("Name")
        this.Type = rawData.getString("Type")
        this.Government = rawData.getString("Government")
        this.RegionID = rawData.getInt("RegionID")
        this.Image = null
    }

    fun setRegionName(rawData:JSONObject){
        this.RegionName = rawData.getString("Name")
    }

    fun setGovernor(rawData: JSONObject){
        this.Governor = _Governor(rawData)
    }

    fun setArmyType(rawData: JSONObject){
        this.ArmyType = _ArmyType(rawData)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(IDPlanet)
        parcel.writeString(Name)
        parcel.writeString(Type)
        parcel.writeString(Government)
        parcel.writeInt(RegionID)
        parcel.writeString(Image)
        parcel.writeString(RegionName)
        parcel.writeParcelable(Governor, flags)
        parcel.writeParcelable(ArmyType, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Planet> {
        override fun createFromParcel(parcel: Parcel): Planet {
            return Planet(parcel)
        }

        override fun newArray(size: Int): Array<Planet?> {
            return arrayOfNulls(size)
        }
    }

}