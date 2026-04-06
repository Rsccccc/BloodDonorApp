package com.example.blooddonorapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ManageRecordsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: DonationRecordsAdapter

    private lateinit var etDate: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnAdd: Button
    private lateinit var rvRecords: RecyclerView
    
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_records)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etDate = findViewById(R.id.etDonationDate)
        etLocation = findViewById(R.id.etLocation)
        btnAdd = findViewById(R.id.btnAddRecord)
        rvRecords = findViewById(R.id.rvRecords)

        // Pre-fill today's date
        updateLabel()

        etDate.setOnClickListener {
            showDatePicker()
        }

        setupRecyclerView()
        loadRecords()

        btnAdd.setOnClickListener {
            addRecord()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateLabel() {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        etDate.setText(sdf.format(calendar.time))
    }

    private fun setupRecyclerView() {
        adapter = DonationRecordsAdapter(emptyList()) { recordId ->
            deleteRecord(recordId)
        }
        rvRecords.layoutManager = LinearLayoutManager(this)
        rvRecords.adapter = adapter
    }

    private fun loadRecords() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("donation_records")
            .whereEqualTo("donorUid", uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading records: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val records = value?.map { doc ->
                    doc.toObject(DonationRecord::class.java).copy(id = doc.id)
                } ?: emptyList()

                // Sort manually by date (string comparison works for yyyy-MM-dd)
                val sortedRecords = records.sortedByDescending { it.date }
                adapter.updateRecords(sortedRecords)
            }
    }

    private fun addRecord() {
        val date = etDate.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val uid = auth.currentUser?.uid ?: return

        if (date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val record = hashMapOf(
            "donorUid" to uid,
            "date" to date,
            "location" to location,
            "timestamp" to Timestamp.now()
        )

        db.collection("donation_records")
            .add(record)
            .addOnSuccessListener {
                etLocation.text.clear()
                Toast.makeText(this, "Record added", Toast.LENGTH_SHORT).show()
                
                // Ask to update profile location
                showUpdateLocationDialog(location)
            }
    }

    private fun showUpdateLocationDialog(newLocation: String) {
        AlertDialog.Builder(this)
            .setTitle("Update Profile Location?")
            .setMessage("Do you want to set your profile location to '$newLocation' so others can find you there?")
            .setPositiveButton("Yes") { _, _ ->
                val uid = auth.currentUser?.uid ?: return@setPositiveButton
                db.collection("users").document(uid).update("city", newLocation)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile location updated!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteRecord(recordId: String) {
        db.collection("donation_records")
            .document(recordId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show()
            }
    }
}