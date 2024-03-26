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
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random


class MapFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Create an instance of SupportMapFragment
        val supportMapFragment = SupportMapFragment.newInstance()

        // Add the SupportMapFragment to the mapContainer in the layout
        childFragmentManager.beginTransaction()
            .add(R.id.mapContainer, supportMapFragment)
            .commit()

        // Set up the map when it's ready
        supportMapFragment.getMapAsync { googleMap ->
            // Launch a coroutine to fetch images from the database
            lifecycleScope.launch(Dispatchers.IO) {
                val appDatabase = AppDatabase.getDatabase(requireContext())
                val images = appDatabase.imageDao().getAllImages()
                withContext(Dispatchers.Main) {
                    images.forEach { image ->
                        val location = LatLng(image.latitude, image.longitude)
                        Log.e("LOKACIJA", location.toString())
                        Log.e("LOKACIJA", BitmapDescriptorFactory.HUE_AZURE.toString())

                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .title(image.description)
                                .position(location)
                                .icon(BitmapDescriptorFactory.defaultMarker(Random.nextInt(0, 360).toFloat()))
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        // Set an OnMarkerClickListener on the GoogleMap object
                        googleMap.setOnMarkerClickListener { markerClicked ->
                            // Check if the clicked marker is the one we added
//                            if (markerClicked.id == marker!!.id) {
//                                // Zoom in on the clicked marker
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
//                            }
                            // Return false to indicate that we have not consumed the event and that we wish
                            // for the default behavior to occur (which is for the camera to move such that the
                            // marker is centered and for the marker's info window to open, if it has one).
                            false
                        }
                    }
                }
            }
        }

        return view
    }


}