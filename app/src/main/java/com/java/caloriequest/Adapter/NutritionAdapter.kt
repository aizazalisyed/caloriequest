package com.java.caloriequest.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.java.caloriequest.R
import com.java.caloriequest.model.NutritionFacts

class NutritionAdapter(private val nutritionList: MutableList<NutritionFacts>) :
    RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {

    // ViewHolder class for the item layout
    inner class NutritionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.itemName)
        val servingSize: TextView = itemView.findViewById(R.id.servingSize)
        val calories: TextView = itemView.findViewById(R.id.calories)
        val fat: TextView = itemView.findViewById(R.id.fat)
        val carb: TextView = itemView.findViewById(R.id.carb)
        val protein: TextView = itemView.findViewById(R.id.protien)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.nutrition_rv_item, parent, false)
        return NutritionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        val nutritionItem = nutritionList[position]

        // Bind data to the views in the ViewHolder
        holder.foodName.text = nutritionItem.foodName
        holder.servingSize.text = nutritionItem.servingSize + " g"
        holder.calories.text = nutritionItem.calories
        holder.fat.text = nutritionItem.Fats + " g"
        holder.carb.text = nutritionItem.carb + " g"
        holder.protein.text = nutritionItem.protein + " g"
    }

    override fun getItemCount(): Int {
        return nutritionList.size
    }

    // Function to update the data in the adapter
    fun updateData(newData: List<NutritionFacts>) {
        nutritionList.clear()
        nutritionList.addAll(newData)
        notifyDataSetChanged()
    }
}
