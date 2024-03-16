package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.w3c.dom.Text
import java.io.File
import java.io.IOException


class SecondFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitue: Double = 0.0
    private var longitute: Double = 0.0

    private fun openGoogleMaps(latitude: Double, longitude: Double) {
//        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
        val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude(My Location)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        mapIntent.setPackage("com.google.android.apps.maps")
//        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
//        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        imageView = view.findViewById(R.id.image_view)
        val imageUri: Uri? = arguments?.getParcelable("uri")
        val imagePath = arguments?.getString("image_path")

        if (imageUri != null) {
            imageView.setImageURI(imageUri)
        } else {
            Log.e("SecondFragment", "Image URI is null")
        }

        if(imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                try {
                    val exif = ExifInterface(file)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }

                    // rotate the image if necessary
                    if (rotation != 0) {
                        val matrix = Matrix()
                        matrix.postRotate(rotation.toFloat())
                        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        imageView.setImageBitmap(rotatedBitmap)
                    } else {
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                Log.e("Brick", "File does not exist: $imagePath")
            }
        }

        if(imagePath != null || imageUri != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    this.latitue = it.latitude
                    this.longitute = it.longitude
                }

                view.findViewById<Button>(R.id.save_image).setOnClickListener {
                    Log.e("P", "PISSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSs")
                    openGoogleMaps(latitue, longitute)
                }
            }



        }

        return view
    }
}
