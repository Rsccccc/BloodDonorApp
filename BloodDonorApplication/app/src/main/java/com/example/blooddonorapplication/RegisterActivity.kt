package com.example.blooddonorapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        rgRole = findViewById(R.id.rgRole)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val role = when (rgRole.checkedRadioButtonId) {
            R.id.rbDonor -> "donor"
            R.id.rbRecipient -> "recipient"
            else -> ""
        }

        if (!validateInput(fullName, email, password, role)) return

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    setLoading(false)
                    showToast("Registration failed. Please try again.")
                    return@addOnSuccessListener
                }

                val userData = hashMapOf(
                    "uid" to uid,
                    "fullName" to fullName,
                    "email" to email,
                    "role" to role,
                    "bloodType" to "",
                    "city" to "",
                    "phone" to "",
                    "isAvailable" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        showToast("Account created successfully")
                        goToDashboard(role)
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        showToast("Failed to save profile: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showToast(e.message ?: "Registration failed")
            }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        password: String,
        role: String
    ): Boolean {
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            etFullName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email"
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return false
        }

        if (role.isEmpty()) {
            showToast("Please select a role")
            return false
        }

        return true
    }

    private fun goToDashboard(role: String) {
        val intent = if (role == "donor") {
            Intent(this, DonorDashboardActivity::class.java)
        } else {
            Intent(this, RecipientDashboardActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}