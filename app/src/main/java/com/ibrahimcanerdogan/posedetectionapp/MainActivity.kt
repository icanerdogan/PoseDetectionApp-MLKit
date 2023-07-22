package com.ibrahimcanerdogan.posedetectionapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.ibrahimcanerdogan.posedetectionapp.databinding.ActivityMainBinding
import com.ibrahimcanerdogan.posedetectionapp.utils.AlertDialog

class MainActivity : AppCompatActivity() {

    private val viewModel = PoseViewModel()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var previewPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        previewPhoto = binding.imageViewPreviewPhoto
        buttonClick()
    }

    private fun buttonClick() {
        // Image from gallery.
        binding.buttonGallery.setOnClickListener {
            if (allPermissionsGranted()) {
                selectImageFromGallery()
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION)
            }
        }
        // Clear image and text.
        binding.buttonClear.setOnClickListener {
            if (previewPhoto.drawable != null){
                AlertDialog.make(
                    context = this,
                    title = "Make a Choice!",
                    message = "Do you confirm?",
                    positive = {
                        previewPhoto.setImageResource(0)
                        binding.textViewChooseGallery.text = resources.getString(R.string.choose_image)
                    },
                    negative = {
                        Toast.makeText(this, "Transaction canceled.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        // Analyse image.
        binding.buttonSend.setOnClickListener {
            if (previewPhoto.drawable == null) {
                Toast.makeText(this, "Preview image is empty.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Pose is being processed...", Toast.LENGTH_SHORT).show()
                viewModel.runPose(this, previewPhoto.drawable.toBitmap())
                viewModel.calculatePose(previewPhoto.drawable.toBitmap())
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainActivity2::class.java)
                    startActivity(intent)
                    finish()
                }, 2000)
            }

        }
    }

    // Gallery
    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                previewPhoto.setImageURI(uri)
                binding.textViewChooseGallery.text = ""
            }
        }

    // Permission
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                selectImageFromGallery()
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 0
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}