package com.example.blooddonorapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DonorDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: DonationRecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_details)

        db = FirebaseFirestore.getInstance()

        val uid = intent.getStringExtra("donor_uid") ?: ""
        val name = intent.getStringExtra("donor_name") ?: ""
        val bloodType = intent.getStringExtra("donor_blood_type") ?: ""
        val city = intent.getStringExtra("donor_city") ?: ""
        val phone = intent.getStringExtra("donor_phone") ?: ""

        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailBloodType).text = "Blood Type: $bloodType"
        findViewById<TextView>(R.id.tvDetailCity).text = "Location: $city"

        findViewById<Button>(R.id.btnCallDonor).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        }

        setupRecyclerView()
        loadHistory(uid)
    }

    private fun setupRecyclerView() {
        val rvHistory = findViewById<RecyclerView>(R.id.rvDonorHistory)
        // Pass null for onDelete since this is read-only
        adapter = DonationRecordsAdapter(emptyList(), null)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter
    }

    private fun loadHistory(uid: String) {
        db.collection("donation_records")
            .whereEqualTo("donorUid", uid)
            .get()
            .addOnSuccessListener { documents ->
                val records = documents.map { doc ->
                    doc.toObject(DonationRecord::class.java).copy(id = doc.id)
                }
                
                // Sort manually by date (string comparison works for yyyy-MM-dd)
                val sortedRecords = records.sortedByDescending { it.date }
                adapter.updateRecords(sortedRecords)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load history: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}