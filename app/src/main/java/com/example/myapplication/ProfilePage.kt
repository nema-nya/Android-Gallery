package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Image
import com.example.myapplication.data.ImageAdapter
import com.example.myapplication.data.ImageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfilePage : Fragment(), SensorEventListener {

    private lateinit var imageDao: ImageDao
    private lateinit var images: List<Image>

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastTime: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f
    private val SHAKE_THRESHOLD = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_list, container, false)

        val db = AppDatabase.getDatabase(requireContext())
        imageDao = db.imageDao()

        fetchImages(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime > 100) {
            val diffTime = currentTime - lastTime
            lastTime = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                val fragmentManager = activity?.supportFragmentManager
                val fragmentTransaction = fragmentManager?.beginTransaction()
                fragmentTransaction?.setCustomAnimations(R.anim.shaky, R.anim.vertical_shake)
                fragmentTransaction?.replace(R.id.flContent, CaptureImage())
                fragmentTransaction?.addToBackStack(null)
                fragmentTransaction?.commit()

                view?.postDelayed({
                }, 100)

            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }



    private fun fetchImages(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            images = imageDao.getAllImages()
            withContext(Dispatchers.Main) {
                Log.e("Images", images.toString())
                displayImages(images.toMutableList(), view, imageDao)
            }
        }
    }

    private fun displayImages(images: MutableList<Image>, view: View, imageD: ImageDao) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = ImageAdapter(images, imageD)
    }

}