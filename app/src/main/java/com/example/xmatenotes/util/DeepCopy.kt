package com.example.xmatenotes.util

import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

fun <T : Serializable> T.deepCopy(obj: T?): T? {
    if (obj == null) return null
    val baos = ByteArrayOutputStream()
    val oos  = ObjectOutputStream(baos)
    oos.writeObject(obj)
    oos.close()
    val bais = ByteArrayInputStream(baos.toByteArray())
    val ois  = ObjectInputStream(bais)
    @Suppress("unchecked_cast")
    return ois.readObject() as T
}

fun <T : Parcelable> deepClone(objectToClone: T): T? {
    var parcel: Parcel? = null
    return try {
        parcel = Parcel.obtain()
        parcel.writeParcelable(objectToClone, 0)
        parcel.setDataPosition(0)
        parcel.readParcelable(objectToClone::class.java.classLoader)
    } finally {
        //it is important to recyle parcel and free up resources once done.
        parcel?.recycle()
    }
}