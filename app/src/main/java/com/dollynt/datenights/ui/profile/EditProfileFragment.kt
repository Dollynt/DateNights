package com.dollynt.datenights.ui.profile

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentEditProfileBinding
import com.dollynt.datenights.model.User
import com.dollynt.datenights.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()
    private val REQUEST_IMAGE_CAPTURE = 1
    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.backIcon.setOnClickListener {
            goBackToProfileFragment()
        }

        // Configurar botão de editar imagem
        binding.buttonEditImage.setOnClickListener {
            openImageSelector()
        }

        // Configurar campo de data de nascimento
        binding.editTextBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonSave.setOnClickListener {
            saveUserData()
        }

        return binding.root
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

    private fun openImageSelector() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageProfile.setImageBitmap(imageBitmap)
        }
    }

    private fun setupGenderDropdown() {
        val genderOptions = listOf("Masculino", "Feminino", "Outro")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.autoCompleteGender.setAdapter(adapter)

        val gender = user?.gender
        if (!gender.isNullOrEmpty()) {
            binding.autoCompleteGender.setText(gender, false)
        } else {
            binding.autoCompleteGender.setHint(R.string.not_defined)
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

                    setupGenderDropdown()

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
