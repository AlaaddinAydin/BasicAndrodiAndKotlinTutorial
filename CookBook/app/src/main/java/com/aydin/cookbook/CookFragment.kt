package com.aydin.cookbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aydin.cookbook.databinding.FragmentCookBinding
import com.google.android.material.snackbar.Snackbar

class CookFragment : Fragment() {
    private var _binding: FragmentCookBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var selectedImagesUri : Uri? = null
    private var selecteImagesBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
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
    }
}