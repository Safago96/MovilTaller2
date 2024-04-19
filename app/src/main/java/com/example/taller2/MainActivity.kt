package com.example.taller2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CONTACTS_PERMISSION_CODE = 1
        private const val PERMISSION_REQUEST_CONTACTS = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contactsBTN = findViewById<ImageButton>(R.id.contactsBtn)
        val cameraBTN = findViewById<ImageButton>(R.id.cameraBtn)
        val mapBTN = findViewById<ImageButton>(R.id.mapBtn)

        contactsBTN.setOnClickListener {askForPermission()}
        mapBTN.setOnClickListener {
            val intent = Intent(this@MainActivity, Maps::class.java)
            startActivity(intent)
        }

        cameraBTN.setOnClickListener{
            val intent = Intent(this@MainActivity, Camera::class.java)
            startActivity(intent)
        }

    }
    fun askForPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_CODE
            )
        } else {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, Contacts::class.java)
                startActivity(intent)
            } else {
                showPermissionDeniedDialog()
            }
        }
    }
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("We cannot continue without this permission. Please change it on your phone settings")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}