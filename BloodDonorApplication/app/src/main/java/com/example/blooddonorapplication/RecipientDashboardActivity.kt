package com.example.blooddonorapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipientDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvWelcome: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnSearchDonors: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipient_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvWelcome = findViewById(R.id.tvWelcome)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        btnSearchDonors = findViewById(R.id.btnSearchDonors)

        loadUserProfile()

        btnSearchDonors.setOnClickListener {
            startActivity(Intent(this, SearchDonorsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val fullName = document.getString("fullName") ?: "Recipient"
                tvWelcome.text = "Welcome, $fullName"
            }
    }
}