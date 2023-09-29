import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.java.caloriequest.R
import com.java.caloriequest.model.NutritionItem

class NutritionDataAdapter(private var nutritionList: List<NutritionItem>) : RecyclerView.Adapter<NutritionDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.nutrition_record_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nutritionItem = nutritionList[position]
        holder.foodNameTextView.text = nutritionItem.name
        holder.caloriesTextView.text = nutritionItem.calories + " kcal"
    }

    override fun getItemCount(): Int {
        return nutritionList.size
    }

    fun updateData(newNutritionList: List<NutritionItem>) {
        nutritionList = newNutritionList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.foodName)
        val caloriesTextView: TextView = itemView.findViewById(R.id.calories)
    }
}
