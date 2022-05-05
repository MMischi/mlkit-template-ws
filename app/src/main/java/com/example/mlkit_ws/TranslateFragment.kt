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
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TranslateFragment : Fragment() {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val langIdentifier = LanguageIdentification.getClient()
    private val downloadConditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    private lateinit var originalTextView: TextView
    private lateinit var langTextView: TextView
    private lateinit var translatedTextView: TextView
    private lateinit var picImageView: ImageView
    private lateinit var launchCam: ActivityResultLauncher<Intent>


    private val hasCameraPermission
        get() = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private var originalText
        get() = originalTextView.text.toString()
        set(v) {
            originalTextView.text = "Original: $v"
        }

    private var langText
        get() = langTextView.text.toString()
        set(v) {
            langTextView.text = "Language: $v"
        }

    private var translatedText
        get() = translatedTextView.text.toString()
        set(v) {
            translatedTextView.text = "Translated: $v"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_translate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalTextView = view.findViewById(R.id.txt_original)
        langTextView = view.findViewById(R.id.txt_lang)
        translatedTextView = view.findViewById(R.id.txt_translated)
        picImageView = view.findViewById(R.id.img_pic)
        view.findViewById<Button>(R.id.btn_translate).setOnClickListener { onTranslateClicked() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted)
                toast("No permission!")
        }.launch(Manifest.permission.CAMERA)

        launchCam = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val img = it.data?.extras?.get("data") as Bitmap?
            if (img != null) {
                picImageView.setImageBitmap(img)
                detectTextIn(img)
            } else
                toast("No picture for you")
        }
    }

    private fun onTranslateClicked() {
        if (hasCameraPermission) {
            launchCam.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } else
            toast("You have no permission here!")
    }

    private fun detectTextIn(bitmap: Bitmap) {
        val img = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(img)
            .addOnSuccessListener {
                originalText = it.text
                identifyLanguage(it.text)
            }
            .addOnFailureListener {
                toast(it.message ?: "Everything went wrong")
            }
    }

    private fun identifyLanguage(text: String) {
        langIdentifier.identifyLanguage(text)
            .addOnSuccessListener {
                langText = it
                translate(text, it)
            }
            .addOnFailureListener {
                toast(it.message ?: "Everything went wrong")
            }
    }

    private fun translate(text: String, lang: String) {
        val translator = makeTranslatorFor(lang)
        toast("Downloading language pack if needed...")
        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener {
                        translatedText = it
                    }
                    .addOnFailureListener {
                        toast(it.message ?: "Everything went wrong")
                    }
            }
            .addOnFailureListener {
                toast(it.message ?: "Everything went wrong")
            }
    }

    private fun makeTranslatorFor(lang: String): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(lang)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        return Translation.getClient(options)
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}