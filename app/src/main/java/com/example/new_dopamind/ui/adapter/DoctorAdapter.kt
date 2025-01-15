package com.example.new_dopamind.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.new_dopamind.R
import com.example.new_dopamind.data.model.Doctor
import com.example.new_dopamind.databinding.ItemDoctorBinding
import java.text.NumberFormat
import java.util.Locale

class DoctorAdapter : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {
    private val doctors = mutableListOf<Doctor>()
    private var onBookClickListener: ((Doctor) -> Unit)? = null

    fun setDoctors(newDoctors: List<Doctor>) {
        doctors.clear()
        doctors.addAll(newDoctors)
        notifyDataSetChanged()
    }

    fun setOnBookClickListener(listener: (Doctor) -> Unit) {
        onBookClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount() = doctors.size

    inner class DoctorViewHolder(private val binding: ItemDoctorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.apply {
                doctorName.text = doctor.name
                specialtyLabel.text = "Clinical psychologist"
                experienceChip.text = doctor.experience
                priceText.text = formatPrice(doctor.price)

                Glide.with(itemView.context)
                    .load(doctor.image)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(doctorImage)

                btnBook.setOnClickListener {
                    onBookClickListener?.invoke(doctor)
                }
            }
        }

        private fun formatPrice(price: String): String {
            return try {
                val numericPrice = price.replace(Regex("[^0-9]"), "").toDouble()
                NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(numericPrice)
            } catch (e: Exception) {
                "Rp 0"
            }
        }
    }
}