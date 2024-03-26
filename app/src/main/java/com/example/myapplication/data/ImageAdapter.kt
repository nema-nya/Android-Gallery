package com.example.myapplication.data

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.R
import com.example.myapplication.SecondFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ImageAdapter(private val images: MutableList<Image>, private val imageDao: ImageDao) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val descView: TextView = itemView.findViewById(R.id.textViewDescription)
        val locationView: TextView = itemView.findViewById(R.id.txtLocation)
        val buttonView: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        val des = images[position].description.toString()
        val location = images[position].latitude.toString() + " " + images[position].longitude.toString()
        Glide.with(holder.itemView.context)
            .load(image.imagePath)
            .into(holder.imageView)
        holder.descView.text = des
        holder.locationView.text = location
        holder.imageView.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:${image.latitude},${image.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(holder.itemView.context.packageManager) != null) {
                holder.itemView.context.startActivity(mapIntent)
            }
        }

        holder.buttonView.setOnClickListener {
            val id = images[position].id
            deleteImage(id, position)
        }

    }

    private fun deleteImage(id: Int, position: Int) {
        val deletedImage = images.removeAt(position)
        GlobalScope.launch {
            imageDao.deleteImageById(id)
        }
        notifyItemRemoved(position)
    }


    override fun getItemCount() = images.size

}
