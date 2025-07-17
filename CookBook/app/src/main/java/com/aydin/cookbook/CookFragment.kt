package com.aydin.cookbook

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aydin.cookbook.databinding.FragmentCookBinding

class CookFragment : Fragment() {
    private var _binding: FragmentCookBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCookBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener { selectImage(it) }
        binding.saveButton.setOnClickListener { saveCook(it) }
        binding.deleteButton.setOnClickListener { deleteCook(it) }

        arguments?.let {
            val info = CookFragmentArgs.fromBundle(it).info

            if (info == "new"){
                binding.deleteButton.isEnabled = false
                binding.nameText.setText("")
                binding.ingredientsText.setText("")
            } else{
                binding.saveButton.isEnabled = false
            }
        }
    }

    fun saveCook (view: View){

    }

    fun deleteCook(view: View){

    }
    fun selectImage(view: View){

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}