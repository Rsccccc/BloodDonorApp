package com.example.blooddonorapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var spinnerBloodType: Spinner
    private lateinit var etCity: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        spinnerBloodType = findViewById(R.id.spinnerProfileBloodType)
        etCity = findViewById(R.id.etProfileCity)
        etPhone = findViewById(R.id.etProfilePhone)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadProfile()

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val bloodType = doc.getString("bloodType") ?: ""
                    val city = doc.getString("city") ?: ""
                    val phone = doc.getString("phone") ?: ""

                    etCity.setText(city)
                    etPhone.setText(phone)

                    val bloodTypes = resources.getStringArray(R.array.blood_types)
                    val index = bloodTypes.indexOf(bloodType)
                    if (index >= 0) {
                        spinnerBloodType.setSelection(index)
                    }
                }
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val bloodType = spinnerBloodType.selectedItem.toString()
        val city = etCity.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (city.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "bloodType" to bloodType,
            "city" to city,
            "phone" to phone
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}