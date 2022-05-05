package com.example.mlkit_ws

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_sample_1.*

class Sample_1 : Fragment() {

    private val REQUEST_CAMERA = 123
    private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

    private lateinit var originalTextView: TextView
    private lateinit var translatedTextView: TextView
    private lateinit var picImageView: ImageView

    private lateinit var checkPermissions: ActivityResultLauncher<String>
    private lateinit var launchCam: ActivityResultLauncher<Intent>


    private val allPermissionsGranted
        get() =
            REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    requireContext(), it
                ) == PackageManager.PERMISSION_GRANTED
            }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_sample_1, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalTextView = view.findViewById(R.id.txt_original)
        translatedTextView = view.findViewById(R.id.txt_translated)
        picImageView = view.findViewById(R.id.img_pic)
        view.findViewById<Button>(R.id.btn_translate).setOnClickListener { onTranslateClicked() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        checkPermissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted)
                    Toast.makeText(context, "Bruh", Toast.LENGTH_SHORT).show()
            }

        launchCam = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val img = it.data?.extras?.get("data") as Bitmap?
            if (img != null)
                picImageView.setImageBitmap(img)
            else
                Toast.makeText(requireContext(), "Very sad, no picture", Toast.LENGTH_SHORT).show()
        }

        checkPermissions.launch(Manifest.permission.CAMERA)
    }

    public fun onTranslateClicked() {
        if (allPermissionsGranted) {
            launchCam.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }
    }

}