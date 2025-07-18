package com.aydin.cookbook.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aydin.cookbook.databinding.FragmentCookBinding
import com.aydin.cookbook.model.Cook
import com.aydin.cookbook.roomdb.CookDAO
import com.aydin.cookbook.roomdb.CookDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class CookFragment : Fragment() {
    private var _binding: FragmentCookBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var selectedImagesUri : Uri? = null
    private var selecteImagesBitmap : Bitmap? = null

    private lateinit var db : CookDatabase
    private lateinit var cookDao : CookDAO

    private val mDisposable = CompositeDisposable()

    private var selectedCook : Cook? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),CookDatabase::class.java,"Cooks").build()
        cookDao = db.cookDao()
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
                selectedCook = null
                binding.deleteButton.isEnabled = false
                binding.nameText.setText("")
                binding.ingredientsText.setText("")
            } else{
                binding.saveButton.isEnabled = false
                val id = CookFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    cookDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseForGetId)
                )

            }
        }
    }

    private fun handleResponseForGetId(cook: Cook) {
        binding.nameText.setText(cook.name)
        binding.ingredientsText.setText(cook.ingredients)
        val bitmap = BitmapFactory.decodeByteArray(cook.image,0,cook.image.size)
        binding.imageView.setImageBitmap(bitmap)
        selectedCook = cook
    }

    fun saveCook (view: View){
        val name = binding.nameText.text.toString()
        val ingredients = binding.ingredientsText.text.toString()

        if (selecteImagesBitmap != null){
            val tinyBitmap = makeBitmapTiny(selecteImagesBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            tinyBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val selectedImageByteArray = outputStream.toByteArray()

            val newCook = Cook(name, ingredients, selectedImageByteArray)

            mDisposable.add(
            cookDao.insert(newCook)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))
        }
    }

    private fun handleResponseForInsert() {
        val actionToMainPage = CookFragmentDirections.actionCookFragmentToListFragment()
        Navigation.findNavController(requireView()).navigate(actionToMainPage)
    }

    private fun makeBitmapTiny(bitmap: Bitmap, maxSize: Int) : Bitmap{
        var height = bitmap.height
        var width = bitmap.width

        val rate : Double = height.toDouble() / width.toDouble()

        if(rate > 1){
            height = maxSize
            val newWidth = maxSize / rate
            width = newWidth.toInt()
        } else {
            width = maxSize
            val newHeight = maxSize * rate
            height = newHeight.toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height,false)
    }

    fun deleteCook(view: View){
        if (selectedCook != null){
            mDisposable.add(
                cookDao.delete(cook = selectedCook!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)
            )
        }
    }
    fun selectImage(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                //İzin Verilmedi
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar göster, kullanıcıdan nedne izin istediğini ona belirt.
                    Snackbar.make(view, "Galeriye ulamamız lazım.", Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin istenecek
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                } else {
                    // izin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // İzin Veridi galeriye git.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //İzin Verilmedi
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snackbar göster, kullanıcıdan nedne izin istediğini ona belirt.
                    Snackbar.make(view, "Galeriye ulamamız lazım.", Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                            "İzin Ver",
                            View.OnClickListener {
                                //izin istenecek
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        ).show()
                } else {
                    // izin istenecek
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                // İzin Veridi galeriye git.
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentfromResult = result.data
                if (intentfromResult != null){
                    selectedImagesUri = intentfromResult.data
                    // URI to Bitmap
                    try{
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedImagesUri!!)
                            selecteImagesBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selecteImagesBitmap)
                        } else {
                            selecteImagesBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedImagesUri)
                            binding.imageView.setImageBitmap(selecteImagesBitmap)
                        }
                    }catch (e : Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result){
                // izin verildi galeriye gidilebilir.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                // İzin verilmedi yapacak bir şey yok
                Toast.makeText(requireContext(),"İzin vermediniz", Toast.LENGTH_LONG)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }
}