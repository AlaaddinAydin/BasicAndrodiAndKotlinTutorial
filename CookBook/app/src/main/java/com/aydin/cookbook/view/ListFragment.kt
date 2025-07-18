package com.aydin.cookbook.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.aydin.cookbook.adapter.CookAdapter
import com.aydin.cookbook.databinding.FragmentListBinding
import com.aydin.cookbook.model.Cook
import com.aydin.cookbook.roomdb.CookDAO
import com.aydin.cookbook.roomdb.CookDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var db : CookDatabase
    private lateinit var cookDao : CookDAO

    private val mDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),CookDatabase::class.java,"Cooks").build()
        cookDao = db.cookDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { addNew(it) }

        binding.cookRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        getAllData()
    }

    private fun getAllData() {
        mDisposable.add(
            cookDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForGetAll)
        )
    }

    private fun handleResponseForGetAll(cooks : List<Cook>){
        val adapter = CookAdapter(cooks)
        binding.cookRecyclerView.adapter = adapter
    }
    fun addNew(view: View){
        val action = ListFragmentDirections.actionListFragmentToCookFragment(info = "new" , id = 0)
        Navigation.findNavController(view).navigate(action)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}