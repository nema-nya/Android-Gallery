package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.view.GestureDetector
import android.view.MotionEvent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.OpenWeatherMapService
import com.example.myapplication.data.WeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity<Fragment : Any> : AppCompatActivity() {

    lateinit var mDrawer: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var nvDrawer: NavigationView
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            // Manage External Storage Permissions Granted
            Log.d("MainActivity", "Manage External Storage Permissions Granted")
        } else {
            Toast.makeText(this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Below Android 11
        if (result.resultCode == Activity.RESULT_OK) {
            Log.v("MainActivity", "OK Result for Permissions")
        }
    }
}


    private val drawerToggle: ActionBarDrawerToggle? = null

    private val STORAGE_PERMISSION_CODE = 23

    private fun requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != 23 ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != 23) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestForStoragePermissions()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1001)
        } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1002)
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1003)
        } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1004)
        } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 1005)
        } else {


            toolbar = findViewById<Toolbar>(R.id.toolbar)

            setSupportActionBar(toolbar)

            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            mDrawer = findViewById<DrawerLayout>(R.id.drawer_layout)

            nvDrawer = findViewById<NavigationView>(R.id.nvView)



            setupDrawerContent(nvDrawer)

            val drawerToggle = setupDrawerToggle()

            drawerToggle!!.isDrawerIndicatorEnabled = true
            drawerToggle.syncState()

            getCurrentLocation()
            loadWeatherData(latitude, longitude)

        }

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                this.latitude = location.latitude
                this.longitude = location.longitude
            }
        }
    }

    private fun loadWeatherData(lat: Double, long: Double) {
        val openWeatherMapService = ApiClient.getInstance().create(OpenWeatherMapService::class.java)
        openWeatherMapService.getCurrentWeatherData(lat, long, "010bc370317c248f931f293ca00adb2a").enqueue(object :
            Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    if (weatherData != null) {
                        Log.e("L", weatherData.name)
                        updateUI(weatherData)
                    }
                } else {
                    Log.e("WeatherData", "Response error: ${response.code()} ${response.message()} ${call.request().url}}")
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                Log.e("WeatherData", "Error: ${t.message}")
            }
        })
    }

    private fun updateUI(weatherData: WeatherData) {
//        cityNameTextView.text = weatherData.cityName
//        temperatureTextView.text = "${weatherData.temperature}°C"
//        weatherDescriptionTextView.text = weatherData.weatherDescription
        // Load weather icon using a library like Picasso or Glide
        val headerView: View = nvDrawer.getHeaderView(0)
        val headerTextView: TextView = headerView.findViewById(R.id.header_text)
        headerTextView.text = "Trenutno vreme od tvoje keve je:"
    }



    private fun setupDrawerToggle(): ActionBarDrawerToggle? {
        return ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open_drawer, R.string.close_drawer)
    }


    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            uncheckItems()
            selectDrawerItem(menuItem)
            true
        }
    }

    fun uncheckItems() {
        for(i in nvDrawer.menu) {
            i.isChecked = false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Pass any configuration change to the drawer toggles
        drawerToggle!!.onConfigurationChanged(newConfig)
    }


    fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        var fragment: Fragment? = null

        val fragmentClass = when (menuItem.itemId) {
            R.id.nav_first_fragment -> FirstFragment()
            R.id.nav_second_fragment -> SecondFragment()
            R.id.nav_third_fragment -> ThirdFragment()
            R.id.nav_map_fragment -> MapFragment()
            else -> FirstFragment()
        }

        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragmentClass).commit()


        // Highlight the selected item has been done by NavigationView
        menuItem.isChecked = true


        title = menuItem.title

        mDrawer.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}