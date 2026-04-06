package com.example.blooddonorapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchDonorsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: DonorsAdapter

    private lateinit var spinnerBloodType: Spinner
    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var rvDonors: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_donors)

        db = FirebaseFirestore.getInstance()

        spinnerBloodType = findViewById(R.id.spinnerBloodType)
        etCity = findViewById(R.id.etSearchCity)
        btnSearch = findViewById(R.id.btnSearch)
        rvDonors = findViewById(R.id.rvDonors)

        setupRecyclerView()

        btnSearch.setOnClickListener {
            searchDonors()
        }
    }

    private fun setupRecyclerView() {
        adapter = DonorsAdapter(emptyList()) { donor ->
            val intent = Intent(this, DonorDetailsActivity::class.java).apply {
                putExtra("donor_uid", donor.uid)
                putExtra("donor_name", donor.name)
                putExtra("donor_blood_type", donor.bloodType)
                putExtra("donor_city", donor.city)
                putExtra("donor_phone", donor.phone)
            }
            startActivity(intent)
        }
        rvDonors.layoutManager = LinearLayoutManager(this)
        rvDonors.adapter = adapter
    }

    private fun searchDonors() {
        val bloodType = spinnerBloodType.selectedItem.toString()
        val city = etCity.text.toString().trim()

        var query: Query = db.collection("users")
            .whereEqualTo("role", "donor")
            .whereEqualTo("bloodType", bloodType)

        // If city is empty, we just query by blood type.
        // Firestore doesn't support easy case-insensitive search without 3rd party or extra fields.
        // We'll just filter by exact match for now to keep it simple.
        if (city.isNotEmpty()) {
            query = query.whereEqualTo("city", city)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val donors = documents.mapNotNull { doc ->
                    val uid = doc.id
                    val name = doc.getString("fullName") ?: ""
                    val bType = doc.getString("bloodType") ?: ""
                    val dCity = doc.getString("city") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    
                    // Basic client-side check if donor is actually "available"
                    // (Optional: if you want to only show available donors)
                    Donor(uid, name, bType, dCity, phone)
                }
                adapter.updateDonors(donors)
                if (donors.isEmpty()) {
                    Toast.makeText(this, "No $bloodType donors found ${if(city.isNotEmpty()) "in $city" else ""}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

data class Donor(
    val uid: String,
    val name: String,
    val bloodType: String,
    val city: String,
    val phone: String
)