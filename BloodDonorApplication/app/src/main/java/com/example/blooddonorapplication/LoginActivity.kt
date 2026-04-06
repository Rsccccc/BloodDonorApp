package com.example.blooddonorapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var btnFacebook: Button
    private lateinit var btnX: Button
    private lateinit var tvGoToRegister: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnFacebook = findViewById(R.id.btnFacebook)
        btnX = findViewById(R.id.btnX)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnGoogle.setOnClickListener {
            openUrl("https://www.google.com")
        }

        btnFacebook.setOnClickListener {
            openUrl("https://www.facebook.com")
        }

        btnX.setOnClickListener {
            openUrl("https://x.com")
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        checkExistingSession()
    }

    private fun checkExistingSession() {
        val currentUser = auth.currentUser ?: return

        setLoading(true)

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                setLoading(false)
                val role = document.getString("role")
                if (role != null) {
                    goToDashboard(role)
                }
            }
            .addOnFailureListener {
                setLoading(false)
            }
    }

    private fun loginUser() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    setLoading(false)
                    showToast("Login failed")
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        setLoading(false)
                        val role = document.getString("role")
                        if (role != null) {
                            showToast("Login successful")
                            goToDashboard(role)
                        } else {
                            showToast("User role not found")
                        }
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        showToast("Failed to load profile: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                showToast(e.message ?: "Login failed")
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etLoginEmail.error = "Email is required"
            etLoginEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.error = "Enter a valid email"
            etLoginEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etLoginPassword.error = "Password is required"
            etLoginPassword.requestFocus()
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
        btnLogin.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        btnFacebook.isEnabled = !isLoading
        btnX.isEnabled = !isLoading
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}