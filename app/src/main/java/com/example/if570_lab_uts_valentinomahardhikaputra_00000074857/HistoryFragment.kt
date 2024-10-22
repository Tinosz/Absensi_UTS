package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

// HistoryFragment.java
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter  // Declare the adapter
    private var entries: MutableList<AttendanceEntry> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_history)
        adapter = HistoryAdapter(entries)  // Initialize the adapter here
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter  // Set the adapter to the RecyclerView

        loadData()  // Now safe to load data and use adapter

        return view
    }

    private fun loadData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val datesRef = db.collection("accounts").document(userId).collection("dates")

        datesRef.get().addOnSuccessListener { dateDocuments ->
            if (dateDocuments.isEmpty) {
                Log.d("HistoryFragment", "No dates found")
                Toast.makeText(context, "No attendance records found.", Toast.LENGTH_SHORT).show()
            } else {
                for (dateDoc in dateDocuments) {
                    val date = dateDoc.id
                    fetchAttendanceDetails(dateDoc.reference, date)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("HistoryFragment", "Error fetching dates: ", exception)
            Toast.makeText(context, "Error fetching attendance data.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchAttendanceDetails(dateRef: DocumentReference, date: String) {
        GlobalScope.launch(Dispatchers.Main) {  // Use the Main dispatcher for UI updates
            try {
                val masukDocuments = dateRef.collection("Absen Masuk").get().await()
                val pulangDocuments = dateRef.collection("Absen Pulang").get().await()

                if (masukDocuments.documents.isNotEmpty() && pulangDocuments.documents.isNotEmpty()) {
                    masukDocuments.documents.forEach { masukDoc ->
                        val masukImageUrl = masukDoc.getString("imageUrl") ?: ""
                        val masukTime = masukDoc.getString("time") ?: ""

                        pulangDocuments.documents.forEach { pulangDoc ->
                            val pulangImageUrl = pulangDoc.getString("imageUrl") ?: ""
                            val pulangTime = pulangDoc.getString("time") ?: ""

                            // Format the date before adding it to the entry
                            val formattedDate = formatDate(date)
                            val entry = AttendanceEntry(formattedDate, masukTime, masukImageUrl, pulangTime, pulangImageUrl)
                            entries.add(entry)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("HistoryFragment", "No complete attendance records found for $date")
                    Toast.makeText(context, "Incomplete attendance data for some dates.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error fetching attendance details: ", e)
                Toast.makeText(context, "Error fetching attendance data.", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun formatDate(originalDate: String): String {
        val originalFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val targetFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val date = originalFormat.parse(originalDate) // Parse the original format
        return targetFormat.format(date) // Format it to the target format
    }
    }




