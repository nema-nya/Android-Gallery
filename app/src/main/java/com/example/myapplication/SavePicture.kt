package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Image
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class SavePicture : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var descText: TextView
    private var latitue: Double = 0.0
    private var longitute: Double = 0.0


    @SuppressLint("MissingPermission", "NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        imageView = view.findViewById(R.id.image_view)
        var imagePath = arguments?.getString("image_path")

        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                try {
                    val exif = ExifInterface(file)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }

                    if (rotation != 0) {
                        val matrix = Matrix()
                        matrix.postRotate(rotation.toFloat())
                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            matrix,
                            true
                        )
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

        if (imagePath != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            descText = view.findViewById(R.id.desc_text)
            var desc = descText.text
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    this.latitue = it.latitude
                    this.longitute = it.longitude
                }

                view.findViewById<Button>(R.id.save_image).setOnClickListener {
                    GlobalScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(requireContext())
                        val image = Image(0, imagePath!!, longitute, latitue, desc.toString())
                        db.imageDao().insert(image)
                    }

                    val newFragment = ProfilePage()
                    val fragmentManager = activity?.supportFragmentManager
                    val fragmentTransaction = fragmentManager?.beginTransaction()
                    fragmentTransaction?.replace(R.id.flContent, newFragment)
                    fragmentTransaction?.commit()
                }
            }


        }

        return view
    }
}
