package com.semauluc.recipes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
class ListeRcyclerAdapter(val yemekListesi: ArrayList<String>, val idListesi :ArrayList <Int> ) : RecyclerView.Adapter <ListeRcyclerAdapter.YemekHolder>() {
    class YemekHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val recycler_row_text: TextView = itemView.findViewById(R.id.recycler_row_text)
   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YemekHolder {
val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent, false)
        return YemekHolder(view)
    }

    override fun getItemCount(): Int {
return  yemekListesi.size
    }

    override fun onBindViewHolder(holder: YemekHolder, position: Int) {
         holder.itemView.recycler_row_text.text = yemekListesi[position]
         holder.itemView.setOnClickListener {
             val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("recyclerdangeldim", idListesi[position])

         }
    }
}