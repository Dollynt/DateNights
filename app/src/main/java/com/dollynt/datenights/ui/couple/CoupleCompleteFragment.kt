package com.dollynt.datenights.ui.couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dollynt.datenights.R
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CoupleCompleteFragment : Fragment() {

    private lateinit var viewModel: CoupleViewModel
    private lateinit var avatar1: ImageView
    private lateinit var name1: TextView
    private lateinit var avatar2: ImageView
    private lateinit var name2: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_couple_complete, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)

        avatar1 = view.findViewById(R.id.avatar1)
        name1 = view.findViewById(R.id.name1)
        avatar2 = view.findViewById(R.id.avatar2)
        name2 = view.findViewById(R.id.name2)
        val leaveCoupleButton = view.findViewById<Button>(R.id.leave_couple_button)

        viewModel.getUsers(onComplete = { users ->
            if (users.isNotEmpty()) {
                val user1 = users[0]
                val user2 = users.getOrNull(1)

                if (isAdded) {
                    updateUserInfo(user1, avatar1, name1)
                    if (user2 != null) {
                        updateUserInfo(user2, avatar2, name2)
                    } else {
                        avatar2.setImageResource(R.drawable.empty_profile)
                        name2.text = getString(R.string.null_name)
                    }
                }

                // Configura o botão de sair do casal
                leaveCoupleButton.setOnClickListener {
                    showLeaveCoupleConfirmationDialog()
                }
            }
        }, onError = { exception ->
            Toast.makeText(context, "Erro ao carregar os usuários: ${exception.message}", Toast.LENGTH_SHORT).show()
        })

        return view
    }

    override fun onResume() {
        super.onResume()

        viewModel.getUsers(onComplete = { users ->
            if (users.isNotEmpty()) {
                val user1 = users[0]
                val user2 = users.getOrNull(1)

                if (isAdded) {
                    updateUserInfo(user1, avatar1, name1)
                    if (user2 != null) {
                        updateUserInfo(user2, avatar2, name2)
                    } else {
                        avatar2.setImageResource(R.drawable.empty_profile)
                        name2.text = getString(R.string.null_name)
                    }
                }
            }
        }, onError = { exception ->
            Toast.makeText(context, "Erro ao atualizar os usuários: ${exception.message}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateUserInfo(user: Map<String, Any>, avatarView: ImageView, nameView: TextView) {
        Glide.with(this)
            .load(user["profilePictureUrl"] as? String)
            .placeholder(R.drawable.empty_profile)
            .into(avatarView)

        nameView.text = user["name"] as? String ?: getString(R.string.null_name)
    }

    private fun showLeaveCoupleConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Tem certeza que deseja sair do casal?")
            .setPositiveButton("Sim") { dialog, id ->
                Firebase.auth.currentUser?.uid?.let { userId ->
                    viewModel.leaveCouple(userId)
                    navigateToNoCoupleScreen()
                }
            }
            .setNegativeButton("Não") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun navigateToNoCoupleScreen() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.coupleContentFrame, CreateOrJoinCoupleFragment())
            .commit()
    }
}
