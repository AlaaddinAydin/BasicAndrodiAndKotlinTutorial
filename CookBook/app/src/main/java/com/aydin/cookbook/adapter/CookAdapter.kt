package com.aydin.cookbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.aydin.cookbook.databinding.RecyclerRowBinding
import com.aydin.cookbook.model.Cook
import com.aydin.cookbook.view.ListFragmentDirections

class CookAdapter(val cookList: List<Cook>) : RecyclerView.Adapter<CookAdapter.CookHolder>() {
    class CookHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CookHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CookHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return cookList.size
    }

    override fun onBindViewHolder(holder: CookHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = cookList[position].name
        holder.itemView.setOnClickListener {
            val actionToSelectedItem = ListFragmentDirections.actionListFragmentToCookFragment(info = "old", id = cookList[position].id)
            Navigation.findNavController(it).navigate(actionToSelectedItem)
        }
    }
}