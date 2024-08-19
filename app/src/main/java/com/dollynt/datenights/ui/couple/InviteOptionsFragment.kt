package com.dollynt.datenights.ui.couple

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class InviteOptionsFragment : Fragment() {

    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_invite_options, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(CoupleViewModel::class.java)

        val inviteLinkTextView = view.findViewById<TextView>(R.id.inviteLinkTextView)
        val inviteCodeTextView = view.findViewById<TextView>(R.id.inviteCodeTextView)

        viewModel.inviteLink.observe(viewLifecycleOwner) { inviteLink ->
            inviteLinkTextView.text = inviteLink
        }

        viewModel.inviteCode.observe(viewLifecycleOwner) { inviteCode ->
            inviteCodeTextView.text = inviteCode
        }

        val copyLinkButton = view.findViewById<ImageButton>(R.id.copyLinkButton)
        val shareLinkButton = view.findViewById<ImageButton>(R.id.shareLinkButton)
        val copyCodeButton = view.findViewById<ImageButton>(R.id.copyCodeButton)
        val deleteCoupleButton = view.findViewById<Button>(R.id.deleteCoupleButton)

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

        return view
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
}
