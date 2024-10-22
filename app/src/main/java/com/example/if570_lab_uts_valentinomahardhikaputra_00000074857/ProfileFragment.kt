package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileFragment : Fragment() {

    private lateinit var nimEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nimEditText = view.findViewById(R.id.nim)
        fullNameEditText = view.findViewById(R.id.full_name)
        saveButton = view.findViewById(R.id.save_button)

        saveButton.isEnabled = false  // Initially disable the save button

        loadUserDetails()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                saveButton.isEnabled = true  // Enable the save button when text changes
            }
        }


        nimEditText.addTextChangedListener(textWatcher)
        fullNameEditText.addTextChangedListener(textWatcher)

        saveButton.setOnClickListener {
            val nim = nimEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            if (nim.isNotEmpty() && fullName.isNotEmpty()) {
                saveUserDetails(nim, fullName)
            } else {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize views
        logoutButton = view.findViewById(R.id.logout_button)

        // Set up listeners
        logoutButton.setOnClickListener {
            logoutUser()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    private fun saveUserDetails(nim: String, fullName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userMap = hashMapOf("full_name" to fullName, "nim" to nim)
        FirebaseFirestore.getInstance().collection("accounts").document(userId)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(context, "User details updated successfully", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = false  // Disable save button after successful save
                sharedViewModel.profileComplete.value = true
                findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating user details: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadUserDetails() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("accounts").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        nimEditText.setText(documentSnapshot.getString("nim"))
                        fullNameEditText.setText(documentSnapshot.getString("full_name"))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading user details: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun logoutUser() {
        // Log out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Create an Intent to start LoginActivity
        val loginIntent = Intent(context, LoginActivity::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear the activity stack
        startActivity(loginIntent)

        // Optionally, if you are in a fragment and need to close the fragment or clear other tasks, you can use:
        activity?.finish()  // Close the current activity if there's no need to return to it
    }

}
