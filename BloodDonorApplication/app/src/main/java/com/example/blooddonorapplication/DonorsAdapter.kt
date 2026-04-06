package com.example.blooddonorapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DonorsAdapter(
    private var donors: List<Donor>,
    private val onDonorClick: (Donor) -> Unit
) : RecyclerView.Adapter<DonorsAdapter.DonorViewHolder>() {

    class DonorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDonorName)
        val tvBloodType: TextView = view.findViewById(R.id.tvDonorBloodType)
        val tvCity: TextView = view.findViewById(R.id.tvDonorCity)
        val tvPhone: TextView = view.findViewById(R.id.tvDonorPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor, parent, false)
        return DonorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donors[position]
        holder.tvName.text = donor.name
        holder.tvBloodType.text = donor.bloodType
        holder.tvCity.text = donor.city
        holder.tvPhone.text = donor.phone
        
        holder.itemView.setOnClickListener {
            onDonorClick(donor)
        }
    }

    override fun getItemCount() = donors.size

    fun updateDonors(newDonors: List<Donor>) {
        donors = newDonors
        notifyDataSetChanged()
    }
}