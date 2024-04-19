package com.example.taller2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapsBinding
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.Date
import kotlin.math.roundToInt
import java.util.Locale
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import android.view.MotionEvent
import org.osmdroid.views.MapView


class Maps : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var bindingMaps: ActivityMapsBinding
    private lateinit var sensorMgr: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var listenerLightSensor: SensorEventListener
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    val RADIUS_OF_EARTH_KM = 6371.0
    private val startPoint = org.osmdroid.util.GeoPoint(4.733571589964837, -74.06630452855659)

    override fun onCreate(savedInstanceState: Bundle?) {

        sensorMgr = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        listenerLightSensor = createLightSensorListener()
        sensorMgr.registerListener(
            listenerLightSensor,
            lightSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = packageName

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        bindingMaps = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(bindingMaps.root)

        bindingMaps.mapView1.setTileSource(TileSourceFactory.MAPNIK)
        bindingMaps.mapView1.setMultiTouchControls(true)

        permissions()
        val addressInput = findViewById<EditText>(R.id.addressInput)
        addressInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val location = getLocationFromAddress(v.text.toString())
                location?.let {
                    updateMarker(it.latitude, it.longitude)
                    showDistance(latitude, longitude, it.latitude, it.longitude)
                    bindingMaps.mapView1.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                    bindingMaps.mapView1.controller.setZoom(20.0)
                }
                true
            } else {
                false
            }
        }
        bindingMaps.mapView1.overlays.add(object : Overlay() {
            override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    getAddressFromLocation(geoPoint.latitude, geoPoint.longitude, mapView.context)
                    showDistance(latitude, longitude, geoPoint.latitude, geoPoint.longitude)
                    return true
                }
                return false
            }
        })
    }

    private fun createLightSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (bindingMaps.mapView1 != null) {
                    if (event.values[0] < 5000) {
                        Log.i("MAPS", " DARK MAP" + event.values[0])
                        bindingMaps.mapView1.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    } else {
                        Log.i("MAPS", " LIGHT MAP" + event.values[0])
                        bindingMaps.mapView1.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    private fun permissions() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            currentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun currentLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000L,
            10f,
            locationListener
        )
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("MapsActivity", "Updated Location: $location")

            var lastLatitude = latitude
            var lastLongitude = longitude
            latitude = location.latitude
            longitude = location.longitude
            Log.i("LISTENER", "Latitude: $latitude and Longitude: $longitude")
            updateMarker(latitude, longitude)
            val dist = distance(lastLatitude, lastLongitude, latitude, longitude)
            Log.d("DISTANCE", "Distance: $dist")
            if (dist > 0.3)
                modifyJSON(latitude, longitude)
        }
    }

    private fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }

    private var marker: Marker? = null
    private fun updateMarker(latitude: Double, longitude: Double) {
        if (marker != null) {
            marker?.let { bindingMaps.mapView1.overlays.remove(it) }
        }

        val geoPoint1 = GeoPoint(latitude, longitude)
        marker = Marker(bindingMaps.mapView1).apply {
            icon = changeIconSize(resources.getDrawable(R.drawable.location))
            position = geoPoint1
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        bindingMaps.mapView1.overlays.add(marker)

        val mapController: IMapController = bindingMaps.mapView1.controller
        mapController.animateTo(geoPoint1)
        mapController.setZoom(18.0)
    }

    private fun changeIconSize(icono: Drawable): Drawable {
        val bitmap = (icono as BitmapDrawable).bitmap
        val bitmapUpdated = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        return BitmapDrawable(resources, bitmapUpdated)
    }

    private fun modifyJSON(latitud: Double, longitud: Double) {
        val locations: JSONArray = JSONArray()
        locations.put(
            Location(
                Date(System.currentTimeMillis()), latitud, longitud
            ).toJSON()
        )
        var output: Writer?
        val filename = "locations.json"
        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.i("FILE", "File location: $file")
            output = BufferedWriter(FileWriter(file))
            output.write(locations.toString())
            output.close()
            Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("FILE", "Failed", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            currentLocation()
        } else {

        }
    }

    override fun onResume() {
        super.onResume()
        bindingMaps.mapView1.onResume()
        val mapController: IMapController = bindingMaps.mapView1.controller
        mapController.setZoom(18.0)
        mapController.setCenter(this.startPoint)
    }

    override fun onPause() {
        super.onPause()
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(locationListener)
        bindingMaps.mapView1.onPause()
    }

    private fun getLocationFromAddress(strAddress: String): Location? {
        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocationName(strAddress, 1)
        if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            val location = Location(LocationManager.GPS_PROVIDER)
            location.latitude = address.latitude
            location.longitude = address.longitude
            Log.d("Geocoder", "Adress found: Lat=${address.latitude}, Long=${address.longitude}")
            return location
        } else {
            Log.d("Geocoder", "Addresses not found")
            return null
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double, context: Context) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0].getAddressLine(0)
                runOnUiThread {
                    addMarker(latitude, longitude, address)
                }
            } else {
                Log.d("Maps", "No address found.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Maps", "Geocoder failed", e)
        }
    }

    private fun addMarker(latitude: Double, longitude: Double, address: String) {
        val marker = Marker(bindingMaps.mapView1)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = address
        bindingMaps.mapView1.overlays.add(marker)
        bindingMaps.mapView1.invalidate()
    }

    private fun showDistance(
        oldLatitude: Double,
        oldLongitude: Double,
        newLatitude: Double,
        newLongitude: Double
    ) {
        val startPoint = GeoPoint(oldLatitude, oldLongitude)
        val endPoint = GeoPoint(newLatitude, newLongitude)
        val distance = startPoint.distanceToAsDouble(endPoint) / 1000.0
        Toast.makeText(this, "Distance: ${String.format("%.2f", distance)} km", Toast.LENGTH_LONG)
            .show()
    }
}
