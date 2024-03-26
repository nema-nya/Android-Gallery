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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Image
import com.example.myapplication.data.ImageAdapter
import com.example.myapplication.data.ImageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ThirdFragment : Fragment() {



    private lateinit var imageDao: ImageDao
    private lateinit var images: List<Image>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_list, container, false)

        val db = AppDatabase.getDatabase(requireContext())
        imageDao = db.imageDao()

        // Fetch images from the database
        fetchImages(view)


        return view
    }

    private fun fetchImages(view: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            images = imageDao.getAllImages()
            withContext(Dispatchers.Main) {
                // Update UI with images
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