package com.dollynt.datenights.ui.profile

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentEditProfileBinding
import com.dollynt.datenights.model.User
import com.dollynt.datenights.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private var user: User? = null
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures")

        setupGenderDropdown()
        loadUserData()

        binding.backIcon.setOnClickListener {
            goBackToProfileFragment()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goBackToProfileFragment()
        }

        binding.buttonEditImage.setOnClickListener {
            showImageOptionsDialog()
        }

        binding.editTextBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonSave.setOnClickListener {
            saveUserData()
        }

        return binding.root
    }

    private fun setupGenderDropdown() {
        val genderOptions = listOf("Masculino", "Feminino", "Outro")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.autoCompleteGender.setAdapter(adapter)
    }

    private fun goBackToProfileFragment() {
        val profileFragment = ProfileFragment()
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_profile, profileFragment)
            .commit()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                binding.editTextBirthdate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showImageOptionsDialog() {
        val options = arrayOf("Tirar Foto", "Escolher da Galeria", "Usar Link da Internet")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecionar Imagem")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> pickImageFromGallery()
                2 -> promptForImageUrl()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun pickImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK)
    }

    private fun promptForImageUrl() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Inserir URL da Imagem")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val url = input.text.toString()
            if (url.isNotEmpty()) {
                setImageFromUrl(url)
            } else {
                Toast.makeText(requireContext(), "URL não pode ser vazio", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun setImageFromUrl(url: String) {
        Glide.with(this).load(url).into(binding.imageProfile)
        user?.profilePictureUrl = url
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    uploadImageToFirebase(imageBitmap)
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    imageUri?.let { uri ->
                        uploadImageToFirebase(uri)
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageReference.child("${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                setImageFromUrl(uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Falha ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val imageRef = storageReference.child("${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                setImageFromUrl(downloadUri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Falha ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                user = userRepository.getUserData(uid)
                user?.let {
                    binding.editTextName.setText(it.name)
                    binding.editTextBirthdate.setText(it.birthdate)

                    val genderToDisplay = if (!it.gender.isNullOrEmpty()) {
                        it.gender
                    } else {
                        "Não definido"
                    }
                    binding.autoCompleteGender.setText(genderToDisplay, false)

                    if (!it.profilePictureUrl.isNullOrEmpty()) {
                        Glide.with(this@EditProfileFragment).load(it.profilePictureUrl).into(binding.imageProfile)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun saveUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val name = binding.editTextName.text.toString()
        val gender = binding.autoCompleteGender.text.toString()
        val birthdate = binding.editTextBirthdate.text.toString()
        val profilePictureUrl = user?.profilePictureUrl

        if (name.isEmpty() || birthdate.isEmpty() || gender.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = User(
            uid = uid,
            email = user?.email,
            name = name,
            birthdate = birthdate,
            gender = gender,
            profilePictureUrl = profilePictureUrl
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                userRepository.saveUser(updatedUser)

                val photoUri = if (profilePictureUrl.isNullOrEmpty()) null else Uri.parse(profilePictureUrl)
                updateProfileInAuth(name, photoUri)

                Toast.makeText(context, "Informações atualizadas com sucesso!", Toast.LENGTH_SHORT).show()

                val profileFragment = ProfileFragment()
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_profile, profileFragment)
                    .commit()

            } catch (e: Exception) {
                val errorMessage = "Erro ao salvar dados do usuário: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun updateProfileInAuth(name: String, photoUri: Uri?) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .apply {
                if (photoUri != null) {
                    setPhotoUri(photoUri)
                }
            }
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(context, "Erro ao atualizar perfil no Firebase Auth", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
