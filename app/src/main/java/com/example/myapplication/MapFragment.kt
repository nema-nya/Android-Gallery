package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Image
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

class MapFragment : Fragment() {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val supportMapFragment = SupportMapFragment.newInstance()


        childFragmentManager.beginTransaction()
            .add(R.id.mapContainer, supportMapFragment)
            .commit()

        supportMapFragment.getMapAsync { googleMap ->
            this.googleMap = googleMap
            observeImages()
        }

        return view
    }

    private fun observeImages() {
        val appDatabase = AppDatabase.getDatabase(requireContext())
        appDatabase.imageDao().getAllImagesLiveData().observe(viewLifecycleOwner) { images ->
            updateMapWithImages(images)
        }
    }

    private fun updateMapWithImages(images: List<Image>) {
        googleMap.clear()
        images.forEach { image ->
            val location = LatLng(image.latitude, image.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .title(image.description)
                    .position(location)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            Random.nextInt(0, 360).toFloat()
                        )
                    )
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            googleMap.setOnMarkerClickListener { _ ->
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                false
            }
        }
    }
}
