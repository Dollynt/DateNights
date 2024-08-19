package com.dollynt.datenights.ui.couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.R
import com.dollynt.datenights.viewmodel.CoupleViewModel

class CoupleFragment : Fragment() {

    private lateinit var viewModel: CoupleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[CoupleViewModel::class.java]
        return inflater.inflate(R.layout.fragment_couple_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isInCouple.observe(viewLifecycleOwner) { isInCouple ->
            if (isInCouple) {
                viewModel.isCoupleComplete.observe(viewLifecycleOwner) { isCoupleComplete ->
                    if (isCoupleComplete) {
                        showFragment(CoupleCompleteFragment())
                    } else {
                        showFragment(InviteOptionsFragment())
                    }
                }
            } else {
                showFragment(CreateOrJoinCoupleFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.coupleContentFrame, fragment)
            .commit()
    }
}
