package com.example.taller2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONException
import org.json.JSONObject
import java.util.Date

class Location(var date: Date, var latitude: Double, var longitude: Double) {
    fun toJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("latitude", latitude)
            obj.put("longitude", longitude)
            obj.put("date", date)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return obj
    }
}