package com.iteratehq.iterate

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iteratehq.iterate.databinding.FragmentPromptBinding
import com.iteratehq.iterate.model.Survey

class PromptFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPromptBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPromptBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        val survey = arguments?.getParcelable<Survey>(SURVEY)
        with(binding) {
            btnClose.setOnClickListener {
                dismiss()
            }
            txtPrompt.text = survey?.prompt?.message
            btnPrompt.text = survey?.prompt?.buttonText
            btnPrompt.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(survey?.color ?: "#7457be")
            )
            btnPrompt.setOnClickListener {
                if (survey != null) {
                    // TODO: show survey
                    // InteractionEvents.SurveyDisplayed(survey)
                }
                dismiss()
            }
        }
    }

    companion object {
        private const val SURVEY = "survey"

        fun newInstance(survey: Survey): PromptFragment {
            val bundle = Bundle().apply {
                putParcelable(SURVEY, survey)
            }
            return PromptFragment().apply {
                arguments = bundle
            }
        }
    }
}
