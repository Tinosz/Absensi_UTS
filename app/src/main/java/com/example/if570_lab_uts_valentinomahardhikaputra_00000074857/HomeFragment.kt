package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857

import android.app.Activity
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
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import android.os.Looper
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var dateMonthYearTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable


    private lateinit var imageView: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var buttonAbsenPulang: Button
    private var imageAbsenMasuk: Bitmap? = null
    private var imageAbsenPulang: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize all views at the start
        dateMonthYearTextView = view.findViewById(R.id.date_month_year)
        timeTextView = view.findViewById(R.id.time)
        imageView = view.findViewById(R.id.centeredImageView)
        val buttonRetake = view.findViewById<Button>(R.id.button_retake)
        val buttonSave = view.findViewById<Button>(R.id.button_save)
        buttonAbsenPulang = view.findViewById<Button>(R.id.button_absen_pulang)

        // Set click listeners and other initializations
        imageView.setOnClickListener {
            dispatchTakePictureIntent()
        }

        buttonRetake.setOnClickListener {
            dispatchTakePictureIntent()  // Always available to retake the current photo
        }

        buttonAbsenPulang.setOnClickListener {
            dispatchTakePictureIntent()  // For taking the "Absen Pulang" photo
        }

        buttonSave.setOnClickListener {
            saveImagesToFirebase()  // Save both images to Firebase
            buttonRetake.visibility = View.GONE  // Hide the retake button
            buttonSave.visibility = View.GONE
        }

        // Initial visibility settings
        buttonAbsenPulang.visibility = View.GONE
        buttonSave.visibility = View.GONE
        buttonRetake.visibility = View.GONE

        setCurrentDateTime()
        checkTodayAttendance()

        return view
    }


    override fun onResume() {
        super.onResume()
        startUpdatingTime()  // Start or resume updating the displayed time
        checkTodayAttendance()  // Check if today's attendance has already been recorded
    }

    override fun onPause() {
        super.onPause()
        stopUpdatingTime()
    }

//    private fun initializeViews(view: View) {
//        dateMonthYearTextView = view.findViewById(R.id.date_month_year)
//        timeTextView = view.findViewById(R.id.time)
//        imageView = view.findViewById(R.id.centeredImageView)
//    }

    private fun startUpdatingTime() {
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                updateCurrentTime()  // Update the time text view
                handler.postDelayed(this, 1000)  // Schedule this runnable again after 1 second
            }
        }
        handler.post(runnable)  // Start the initial runnable immediately
    }

    private fun stopUpdatingTime() {
        handler.removeCallbacks(runnable)  // Stop the runnable from being called
    }

    private fun updateCurrentTime() {
        val currentTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        timeTextView.text = timeFormat.format(currentTime.time)
    }

    private fun setCurrentDateTime() {
        val currentDate = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        dateMonthYearTextView.text = dateFormat.format(currentDate.time)
        timeTextView.text = timeFormat.format(currentDate.time)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            // Permission already granted, proceed with camera intent
            dispatchTakePictureIntent()
        }
    }


    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()  // Request permission if not granted
        } else {
            // Proceed to launch camera intent
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)

            if (imageAbsenMasuk == null) {
                imageAbsenMasuk = imageBitmap
                imageView.setImageBitmap(imageBitmap)
                view?.findViewById<TextView>(R.id.textView_absen_masuk_time)?.apply {
                    text = "Waktu Absen Masuk: $currentTime"
                    visibility = View.VISIBLE
                }
                view?.findViewById<Button>(R.id.button_retake)?.visibility = View.VISIBLE
                view?.findViewById<Button>(R.id.button_absen_pulang)?.visibility = View.VISIBLE
                view?.findViewById<Button>(R.id.button_save)?.visibility = View.GONE  // Initially hide the save button
            } else {
                imageAbsenPulang = imageBitmap
                view?.findViewById<ImageView>(R.id.centeredImageViewPulang)?.apply {
                    setImageBitmap(imageBitmap)
                    visibility = View.VISIBLE
                }
                view?.findViewById<TextView>(R.id.textView_absen_pulang_time)?.apply {
                    text = "Waktu Absen Pulang: $currentTime"
                    visibility = View.VISIBLE
                }
                view?.findViewById<Button>(R.id.button_absen_pulang)?.visibility = View.GONE
                view?.findViewById<Button>(R.id.button_save)?.visibility = View.VISIBLE  // Show save button when second image is taken
            }
        }
    }





    private fun saveImagesToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)

        val masukTime = view?.findViewById<TextView>(R.id.textView_absen_masuk_time)?.text.toString().substringAfter(":").trim()
        val pulangTime = view?.findViewById<TextView>(R.id.textView_absen_pulang_time)?.text.toString().substringAfter(":").trim()

        imageAbsenMasuk?.let {
            uploadImageToFirebase(it, "Absen Masuk", userId, dateStamp, masukTime)
        }
        imageAbsenPulang?.let {
            uploadImageToFirebase(it, "Absen Pulang", userId, dateStamp, pulangTime)
        }
    }

    private fun uploadImageToFirebase(image: Bitmap, attendanceType: String, userId: String, dateStamp: String, time: String) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()
        val imageFileName = "$attendanceType-${UUID.randomUUID()}.jpg"
        val storageRef = FirebaseStorage.getInstance().reference.child("$userId/$dateStamp/$imageFileName")


        storageRef.putBytes(imageData).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageDetailsToFirestore(userId, dateStamp, imageUrl, attendanceType, time)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get image URL after upload: $attendanceType", Toast.LENGTH_SHORT).show()
                updateButtonVisibility(false)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image: $attendanceType", Toast.LENGTH_SHORT).show()
            updateButtonVisibility(false)
        }
    }


    private fun saveImageDetailsToFirestore(userId: String, dateStamp: String, imageUrl: String, attendanceType: String, time: String) {
        // Reference to the user's specific date document
        val dateDocRef = FirebaseFirestore.getInstance().collection("accounts").document(userId)
            .collection("dates").document(dateStamp)

        // Set or update the id field in the date document
        val dateDetails = hashMapOf(
            "id" to dateStamp  // Adding or updating the 'id' field with the dateStamp
        )
        dateDocRef.set(dateDetails, SetOptions.merge()).addOnSuccessListener {
            // Continue to save the attendance details in a specific collection under the date document
            val docRef = dateDocRef.collection(attendanceType).document()
            val imageDetails = hashMapOf(
                "time" to time,
                "imageUrl" to imageUrl
            )
            docRef.set(imageDetails).addOnSuccessListener {
                Toast.makeText(context, "Image data and time saved successfully for $attendanceType on $dateStamp", Toast.LENGTH_SHORT).show()
                updateButtonVisibility(true)
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to save image data for $attendanceType on $dateStamp", Toast.LENGTH_SHORT).show()
                updateButtonVisibility(false)

            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to update date document with id for $dateStamp", Toast.LENGTH_SHORT).show()
            updateButtonVisibility(false)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the camera intent
                dispatchTakePictureIntent()
            } else {
                // Permission denied, show a message
                Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun checkTodayAttendance() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)

        val dateDocRef = FirebaseFirestore.getInstance().collection("accounts").document(userId)
            .collection("dates").document(dateStamp)

        val masukRef = dateDocRef.collection("Absen Masuk")
        val pulangRef = dateDocRef.collection("Absen Pulang")

        var checkCount = 0
        var attendanceTaken = false

        val checkAndUpdateUI = {
            checkCount++
            if (checkCount == 2) {
                if (attendanceTaken) {
                    view?.findViewById<TextView>(R.id.textView_already_attended)?.apply {
                        visibility = View.VISIBLE
                    }
                    imageView.visibility = View.GONE
                    view?.findViewById<ImageView>(R.id.centeredImageView)?.visibility = View.GONE
                } else {
                    imageView.visibility = View.VISIBLE
                    view?.findViewById<TextView>(R.id.textView_already_attended)?.visibility = View.GONE
                    view?.findViewById<ImageView>(R.id.centeredImageView)?.visibility = View.VISIBLE
                }
            }
        }

        masukRef.limit(1).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                attendanceTaken = true
            }
            checkAndUpdateUI()
        }.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error checking Absen Masuk: ${e.message}")
            checkAndUpdateUI()
        }

        pulangRef.limit(1).get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                attendanceTaken = true
            }
            checkAndUpdateUI()
        }.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error checking Absen Pulang: ${e.message}")
            checkAndUpdateUI()
        }
    }
    private fun updateButtonVisibility(operationSuccessful: Boolean) {
        val visibility = if (operationSuccessful) View.GONE else View.VISIBLE
        view?.findViewById<Button>(R.id.button_retake)?.visibility = visibility
        view?.findViewById<Button>(R.id.button_save)?.visibility = visibility
    }


    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 1
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
