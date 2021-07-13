package com.iteratehq.iterate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iteratehq.iterate.databinding.PromptBinding
import com.iteratehq.iterate.model.Survey

class Prompt : BottomSheetDialogFragment() {

    private lateinit var binding: PromptBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PromptBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val SURVEY = "survey"

        fun newInstance(survey: Survey): Prompt {
            val bundle = Bundle().apply {
                putParcelable(SURVEY, survey)
            }
            return Prompt().apply {
                arguments = bundle
            }
        }
    }
}
