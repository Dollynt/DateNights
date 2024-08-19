package com.dollynt.datenights.ui.couple

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentCoupleBinding
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CoupleFragment : Fragment() {

    private var _binding: FragmentCoupleBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoupleBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isInCouple.observe(viewLifecycleOwner) { isInCouple ->
            updateLayout(isInCouple)
        }

        viewModel.isCoupleComplete.observe(viewLifecycleOwner) { isCoupleComplete ->
            handleCoupleComplete(isCoupleComplete)
        }
    }

    private fun updateLayout(isInCouple: Boolean) {
        val contentFrame = binding.coupleContentFrame
        val layoutInflater = LayoutInflater.from(context)

        contentFrame.removeAllViews()

        if (!isInCouple) {
            showCreateOrJoinCouple(layoutInflater, contentFrame)
        }
    }

    private fun handleCoupleComplete(isCoupleComplete: Boolean) {
        val contentFrame = binding.coupleContentFrame
        val layoutInflater = LayoutInflater.from(context)

        contentFrame.removeAllViews()

        if (isCoupleComplete) {
            showCoupleComplete(layoutInflater, contentFrame)
        } else {
            showInviteOptions(layoutInflater, contentFrame)
        }
    }

    private fun showCoupleComplete(layoutInflater: LayoutInflater, contentFrame: ViewGroup) {
        val coupleCompleteView = layoutInflater.inflate(R.layout.view_couple_complete, contentFrame, false)

        viewModel.getUsers(onComplete = { users ->
            if (users.isNotEmpty()) {
                // Debug: Certifique-se de que os usuários foram carregados
                println("Usuários carregados: $users")

                val user1 = users[0]
                val user2 = users.getOrNull(1)

                // Referências às Views do layout inflado
                val avatar1 = coupleCompleteView.findViewById<ImageView>(R.id.avatar1)
                val name1 = coupleCompleteView.findViewById<TextView>(R.id.name1)
                val avatar2 = coupleCompleteView.findViewById<ImageView>(R.id.avatar2)
                val name2 = coupleCompleteView.findViewById<TextView>(R.id.name2)
                val leaveCoupleButton = coupleCompleteView.findViewById<Button>(R.id.leave_couple_button)

                // Carrega o avatar e o nome do primeiro usuário
                Glide.with(this)
                    .load(user1["profilePictureUrl"] as? String)
                    .placeholder(R.drawable.empty_profile)
                    .into(avatar1)

                name1.text = user1["name"] as? String ?: getString(R.string.null_name)

                // Carrega o avatar e o nome do segundo usuário, se existir
                if (user2 != null) {
                    Glide.with(this)
                        .load(user2["profilePictureUrl"] as? String)
                        .placeholder(R.drawable.empty_profile)
                        .into(avatar2)

                    name2.text = user2["name"] as? String ?: getString(R.string.null_name)
                } else {
                    avatar2.setImageResource(R.drawable.empty_profile)
                    name2.text = getString(R.string.null_name)
                }

                // Configura o botão de sair do casal
                leaveCoupleButton.setOnClickListener {
                    Firebase.auth.currentUser?.uid?.let { it1 -> viewModel.leaveCouple(it1) }
                }

                // Adiciona a view inflada ao contentFrame
                contentFrame.addView(coupleCompleteView)
            }
        }, onError = { exception ->
            Toast.makeText(context, "Erro ao carregar os usuários: ${exception.message}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun showInviteOptions(layoutInflater: LayoutInflater, contentFrame: ViewGroup) {
        val inviteOptionsView = layoutInflater.inflate(R.layout.view_invite_options, contentFrame, false)
        contentFrame.addView(inviteOptionsView)

        val inviteLinkTextView = inviteOptionsView.findViewById<TextView>(R.id.inviteLinkTextView)
        val inviteCodeTextView = inviteOptionsView.findViewById<TextView>(R.id.inviteCodeTextView)

        viewModel.inviteLink.observe(viewLifecycleOwner) { inviteLink ->
            inviteLinkTextView.text = inviteLink
        }

        viewModel.inviteCode.observe(viewLifecycleOwner) { inviteCode ->
            inviteCodeTextView.text = inviteCode
        }

        val copyLinkButton = inviteOptionsView.findViewById<ImageButton>(R.id.copyLinkButton)
        val shareLinkButton = inviteOptionsView.findViewById<ImageButton>(R.id.shareLinkButton)
        val copyCodeButton = inviteOptionsView.findViewById<ImageButton>(R.id.copyCodeButton)
        val deleteCoupleButton = inviteOptionsView.findViewById<Button>(R.id.deleteCoupleButton)

        copyLinkButton.setOnClickListener {
            copyToClipboard(inviteLinkTextView.text.toString(), "Link copiado!")
        }

        shareLinkButton.setOnClickListener {
            shareLink(inviteLinkTextView.text.toString())
        }

        copyCodeButton.setOnClickListener {
            copyToClipboard(inviteCodeTextView.text.toString(), "Código copiado!")
        }

        deleteCoupleButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showCreateOrJoinCouple(layoutInflater: LayoutInflater, contentFrame: ViewGroup) {
        val createOrJoinView = layoutInflater.inflate(R.layout.view_create_or_join_couple, contentFrame, false)

        // Usando TransitionManager para aplicar animações entre as transições de layout
        TransitionManager.beginDelayedTransition(contentFrame)
        contentFrame.removeAllViews()
        contentFrame.addView(createOrJoinView)

        val createCoupleButton = createOrJoinView.findViewById<Button>(R.id.createCoupleButton)
        val showJoinCoupleLayoutButton = createOrJoinView.findViewById<Button>(R.id.showJoinCoupleLayoutButton)

        createCoupleButton.setOnClickListener {
            viewModel.createCouple(Firebase.auth.currentUser?.uid ?: "")
        }

        showJoinCoupleLayoutButton.setOnClickListener {
            showJoinCoupleScreen(layoutInflater, contentFrame)
        }
    }

    private fun showJoinCoupleScreen(layoutInflater: LayoutInflater, contentFrame: ViewGroup) {
        val joinCoupleView = layoutInflater.inflate(R.layout.view_join_couple, contentFrame, false)

        // Usando TransitionManager para aplicar animações entre as transições de layout
        TransitionManager.beginDelayedTransition(contentFrame)
        contentFrame.removeAllViews()
        contentFrame.addView(joinCoupleView)

        val joinCoupleButton = joinCoupleView.findViewById<Button>(R.id.joinCoupleButton)
        val joinCoupleCodeInput = joinCoupleView.findViewById<EditText>(R.id.joinCoupleCodeInput)
        val backIcon = joinCoupleView.findViewById<ImageView>(R.id.back_icon)

        backIcon.setOnClickListener {
            TransitionManager.beginDelayedTransition(contentFrame)
            showCreateOrJoinCouple(layoutInflater, contentFrame)
        }

        joinCoupleButton.setOnClickListener {
            val code = joinCoupleCodeInput.text.toString()
            viewModel.joinCouple(Firebase.auth.currentUser?.uid ?: "", code)
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Tem certeza que deseja excluir o casal?")
            .setPositiveButton("Sim") { dialog, id ->
                val userId = Firebase.auth.currentUser?.uid
                if (userId != null) {
                    viewModel.deleteCouple(userId)
                }
            }
            .setNegativeButton("Não") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun copyToClipboard(text: String, toastMessage: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    private fun shareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(Intent.createChooser(intent, "Compartilhar link"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
