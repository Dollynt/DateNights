package com.dollynt.datenights.ui.couple

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.databinding.FragmentCoupleBinding
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
        viewModel = ViewModelProvider(this).get(CoupleViewModel::class.java)

        val user = Firebase.auth.currentUser

        viewModel.checkCoupleStatus(user?.uid ?: "")

        viewModel.isCoupleCreated.observe(viewLifecycleOwner) { isCoupleCreated ->
            val contentFrame = binding.coupleContentFrame
            val layoutInflater = LayoutInflater.from(context)
            contentFrame.removeAllViews()

            if (isCoupleCreated) {
                val inviteOptionsView = layoutInflater.inflate(R.layout.view_invite_options, contentFrame, false)
                contentFrame.addView(inviteOptionsView)

                val inviteLinkTextView = inviteOptionsView.findViewById<TextView>(R.id.inviteLinkTextView)
                val inviteCodeTextView = inviteOptionsView.findViewById<TextView>(R.id.inviteCodeTextView)
                val copyLinkButton = inviteOptionsView.findViewById<ImageButton>(R.id.copyLinkButton)
                val shareLinkButton = inviteOptionsView.findViewById<ImageButton>(R.id.shareLinkButton)
                val copyCodeButton = inviteOptionsView.findViewById<ImageButton>(R.id.copyCodeButton)

                viewModel.inviteLink.observe(viewLifecycleOwner) { inviteLink ->
                    inviteLinkTextView.text = inviteLink
                }

                viewModel.inviteCode.observe(viewLifecycleOwner) { inviteCode ->
                    inviteCodeTextView.text = inviteCode
                }

                copyLinkButton.setOnClickListener {
                    copyToClipboard(inviteLinkTextView.text.toString(), "Link copiado!")
                }

                shareLinkButton.setOnClickListener {
                    shareLink(inviteLinkTextView.text.toString())
                }

                copyCodeButton.setOnClickListener {
                    copyToClipboard(inviteCodeTextView.text.toString(), "Código copiado!")
                }
            } else {
                val createOrJoinView = layoutInflater.inflate(R.layout.view_create_or_join_couple, contentFrame, false)
                contentFrame.addView(createOrJoinView)

                val createCoupleButton = createOrJoinView.findViewById<Button>(R.id.createCoupleButton)
                val showJoinCoupleLayoutButton = createOrJoinView.findViewById<Button>(R.id.showJoinCoupleLayoutButton)

                createCoupleButton.setOnClickListener {
                    viewModel.createCouple(user?.uid ?: "")
                }

                showJoinCoupleLayoutButton.setOnClickListener {
                    val joinCoupleView = layoutInflater.inflate(R.layout.view_join_couple, contentFrame, false)
                    contentFrame.removeAllViews()
                    contentFrame.addView(joinCoupleView)

                    val joinCoupleButton = joinCoupleView.findViewById<Button>(R.id.joinCoupleButton)
                    val joinCoupleCodeInput = joinCoupleView.findViewById<EditText>(R.id.joinCoupleCodeInput)

                    joinCoupleButton.setOnClickListener {
                        val code = joinCoupleCodeInput.text.toString()
                        viewModel.joinCouple(user?.uid ?: "", code)
                    }
                }
            }
        }

        return binding.root
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
