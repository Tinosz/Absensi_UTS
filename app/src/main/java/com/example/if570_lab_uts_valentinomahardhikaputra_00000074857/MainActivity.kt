package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857

// MainActivity.kt
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    val viewModel: SharedViewModel by viewModels()
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        viewModel.profileComplete.observe(this) { isComplete ->
            if (isComplete) {
                enableNavigation()
            } else {
                disableNavigationExceptProfile()
            }
        }

        checkUserDetails() // First check
    }


    private fun checkUserDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("accounts").document(userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.exists()) {
                        val fullName = task.result.getString("full_name")
                        val nim = task.result.getString("nim")
                        if (fullName.isNullOrEmpty() || nim.isNullOrEmpty()) {
                            disableNavigationExceptProfile()
                        } else {
                            enableNavigation()
                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch user details", task.exception)
                        disableNavigationExceptProfile()
                    }
                }
        } else {
            disableNavigationExceptProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        checkUserDetails()
    }


    private fun disableNavigationExceptProfile() {
        navView.menu.forEach {
            it.isEnabled = false
            Log.d("Navigation", "Disabling: ${it.title}")
        }
        navView.menu.findItem(R.id.nav_profile).isEnabled = true
        navView.selectedItemId = R.id.nav_profile  // Force navigation to Profile
//        Toast.makeText(this, "Please complete your profile to access other features.", Toast.LENGTH_LONG).show()
        Log.d("Navigation", "Profile is enabled, others disabled")
    }

    private fun enableNavigation() {
        navView.menu.forEach {
            it.isEnabled = true
            Log.d("Navigation", "Enabling: ${it.title}")
        }
        Log.d("Navigation", "All navigation items enabled")
    }

}
