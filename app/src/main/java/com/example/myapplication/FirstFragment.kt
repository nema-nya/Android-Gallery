package com.example.myapplication

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class FirstFragment : Fragment() {

    lateinit var buttonFromGalary: Button
    lateinit var buttonFromCamera: Button
    private lateinit var pickImage: ActivityResultLauncher<Intent>
    private lateinit var pickImageFromCamera: ActivityResultLauncher<Intent>
    private lateinit var currentPhotoPath: String
    private lateinit var imageUri: Uri

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImageFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fragmentB = SecondFragment()
                val bundle = Bundle()
                bundle.putString("image_path", currentPhotoPath)
                fragmentB.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.flContent, fragmentB)
                    .commit()
            }
        }
        pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    val newFragment = SecondFragment()
                    val bundle = Bundle()
                    bundle.putParcelable("uri", selectedImageUri)
                    newFragment.arguments = bundle
                    val fragmentManager = activity?.supportFragmentManager
                    val fragmentTransaction = fragmentManager?.beginTransaction()
                    fragmentTransaction?.replace(R.id.flContent, newFragment)
                    fragmentTransaction?.commit()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)
        buttonFromCamera = view.findViewById<Button>(R.id.button_capture_from_camera)
        buttonFromGalary = view.findViewById<Button>(R.id.button_add_from_galery)

        buttonFromGalary.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        buttonFromCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            pickImageFromCamera.launch(cameraIntent)
        }

        val photoFile: File = createImageFile()
        imageUri = FileProvider.getUriForFile(requireContext(), "com.example.myapplication.fileprovider", photoFile)

        return view
    }
}
