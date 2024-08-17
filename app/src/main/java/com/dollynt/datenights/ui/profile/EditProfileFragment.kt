package com.dollynt.datenights.ui.profile

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentEditProfileBinding
import java.util.*

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Carregar dados do usuário
        loadUserData()

        binding.backIcon.setOnClickListener() {
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
        // Cria uma nova instância do ProfileFragment
        val profileFragment = ProfileFragment()

        // Realiza a transação de fragmento para substituir o EditProfileFragment pelo ProfileFragment
        parentFragmentManager.beginTransaction()
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

    private fun loadUserData() {
        // Carregar os dados do usuário para preencher os campos
        // Exemplo:
        // binding.editTextName.setText(user.name)
        // binding.spinnerGender.setSelection(...)
        // binding.editTextBirthdate.setText(user.birthdate)
    }

    private fun saveUserData() {
        val name = binding.editTextName.text.toString()
        val gender = binding.spinnerGender.selectedItem.toString()
        val birthdate = binding.editTextBirthdate.text.toString()

        if (name.isEmpty() || birthdate.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos corretamente", Toast.LENGTH_SHORT).show()
        } else {
            // Salvar os dados do usuário
            // Exemplo:
            // user.updateProfile(name, gender, birthdate)
            Toast.makeText(context, "Informações atualizadas com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
