package com.example.blooddonorapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DonationRecordsAdapter(
    private var records: List<DonationRecord>,
    private val onDeleteClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<DonationRecordsAdapter.RecordViewHolder>() {

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvRecordDate)
        val tvLocation: TextView = view.findViewById(R.id.tvRecordLocation)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteRecord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.tvDate.text = record.date
        holder.tvLocation.text = record.location
        
        // Hide delete button if no callback is provided (read-only mode)
        if (onDeleteClick == null) {
            holder.btnDelete.visibility = View.GONE
        } else {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick.invoke(record.id) }
        }
    }

    override fun getItemCount() = records.size

    fun updateRecords(newRecords: List<DonationRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}