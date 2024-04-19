package com.example.taller2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import javax.microedition.khronos.opengles.GL10

class Camera : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val GALLERY_PERMISSION_CODE = 2
    }

    private lateinit var activityResultLauncherCamera: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherGallery: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val galleryBTN1 = findViewById<Button>(R.id.galleryBtn)
        val cameraBTN1 = findViewById<Button>(R.id.cameraBtn)
        val image = findViewById<ImageView>(R.id.uploadedImage)

        resultLauncherCameraActivate(image)
        resultLauncherGallery(image)

        galleryBTN1.setOnClickListener {
            galleryPermission()
        }

        cameraBTN1.setOnClickListener {
            cameraPermission()
        }
    }
    private fun galleryPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_CODE
            )
        } else {
            openGallery()
        }
    }
    private fun cameraPermission(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permission not granted by user", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Permission not granted by user", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun openCamera(){
        val intentCamara = Intent("android.media.action.IMAGE_CAPTURE")
        activityResultLauncherCamera.launch(intentCamara)
    }

    private fun resultLauncherCameraActivate(image: ImageView){
        activityResultLauncherCamera = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitMapImagen = result.data?.extras?.get("data") as? Bitmap
                if(bitMapImagen != null){
                    Glide.with(this).load(bitMapImagen).into(image)
                }
            }else{
                Toast.makeText(this, "Image could not be loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery(){
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncherGallery.launch(intentGallery)
    }

private fun resultLauncherGallery(imageView2: ImageView){
    activityResultLauncherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            val uri = result.data?.data
            Glide.with(this).load(uri).into(imageView2)
        }else{
            Toast.makeText(this, "Image could not be loaded", Toast.LENGTH_SHORT).show()
        }
    }
}
}