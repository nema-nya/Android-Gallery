package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.OpenWeatherMapService
import com.example.myapplication.data.WeatherData
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity: AppCompatActivity() {

    private lateinit var mDrawer: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var nvDrawer: NavigationView

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Log.d("MA", "b1")
            }
        } else {
            if (result.resultCode == Activity.RESULT_OK) {
                Log.v("MA", "b2")
            }
        }
    }


    private val drawerToggle: ActionBarDrawerToggle? = null
    private fun requestPermissionsIfNotGranted(permissions: Array<String>, requestCode: Int) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, requestCode)
        }
    }

    private fun requestForAllPermissions() {
        requestPermissionsIfNotGranted(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WAKE_LOCK
            ),
            1
        )
    }

    private fun requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
            } else {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    storageActivityResultLauncher.launch(intent)
                } catch (e: Exception) {
                }
            }
        } else {
            requestPermissionsIfNotGranted(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestForStoragePermissions()
        requestForAllPermissions()

        val fragmentManager: FragmentManager = supportFragmentManager

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDrawer = findViewById(R.id.drawer_layout)
        nvDrawer = findViewById(R.id.nvView)
        setupDrawerContent(nvDrawer)

        val drawerToggle = setupDrawerToggle()
        drawerToggle!!.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        loadWeatherData()

        val fragment = if (resources.getBoolean(R.bool.is_tablet)) {
            Log.e("ERR", resources.getBoolean(R.bool.is_tablet).toString())
            CaptureImage()
        } else {
            ProfilePage()
        }

        fragmentManager.beginTransaction()
            .replace(R.id.flContent, fragment)
            .apply {
                if (resources.getBoolean(R.bool.is_tablet)) {
                    replace(R.id.flContentTwo, MapFragment())
                }
            }
            .commit()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(callback: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            callback(location)
        }
    }



    private fun loadWeatherData() {
        getCurrentLocation { location ->
            Log.e("LOC", location.toString())
            val openWeatherMapService =
                ApiClient.getInstance().create(OpenWeatherMapService::class.java)
            openWeatherMapService.getCurrentWeatherData(
                location!!.latitude,
                location!!.longitude,
                "",
                "metric"
            ).enqueue(object :
                Callback<WeatherData> {
                override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                    if (response.isSuccessful) {
                        val weatherData = response.body()
                        if (weatherData != null) {
                            Log.e("L", weatherData.toString())
                            Log.e("L", "$location.latitude $location.longitude")
                            updateUI(weatherData)
                        }
                    } else {
                        Log.e(
                            "WeatherData",
                            "Response error: ${response.code()} ${response.message()} ${call.request().url}}"
                        )
                    }
                }

                override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                    Log.e("WeatherData", "Error: ${t.message}")
                }
            })
        }
        }

    private fun updateUI(weatherData: WeatherData) {
        val headerView: View = nvDrawer.getHeaderView(0)
        val headerTextView: TextView = headerView.findViewById(R.id.header_text)
        headerTextView.text = "the current weather is ${weatherData.weather[0].description} with the temperature of ${weatherData.main.temp}"
    }



    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open_drawer, R.string.close_drawer)
    }


    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }


    private fun selectDrawerItem(menuItem: MenuItem) {
        val fragmentClass = when {
            resources.getBoolean(R.bool.is_tablet) -> when (menuItem.itemId) {
                R.id.nav_second_fragment -> CaptureImage()
                R.id.nav_third_fragment -> ProfilePage()
                else -> ProfilePage()
            }
            else -> when (menuItem.itemId) {
                R.id.nav_second_fragment -> CaptureImage()
                R.id.nav_third_fragment -> ProfilePage()
                R.id.nav_map_fragment -> MapFragment()
                else -> ProfilePage()
            }
        }

        val fragmentManager: FragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.flContent, fragmentClass).commit()

        title = menuItem.title

        mDrawer.closeDrawers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        loadWeatherData()
    }
}