package com.example.blooddonorapplication

import com.google.firebase.Timestamp

data class DonationRecord(
    val id: String = "",
    val donorUid: String = "",
    val date: String = "",
    val location: String = "",
    val timestamp: Timestamp? = null
)