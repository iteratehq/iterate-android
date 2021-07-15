package com.iteratehq.iterate.view

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iteratehq.iterate.databinding.PromptViewBinding
import com.iteratehq.iterate.model.Survey

class PromptView : BottomSheetDialogFragment() {

    interface PromptListener {
        fun onDismiss()
        fun onPromptButtonClick(survey: Survey)
    }

    private lateinit var binding: PromptViewBinding
    private var listener: PromptListener? = null
    private var promptButtonClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PromptViewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        // Call listener only when the prompt is dismissed not due to clicking on the prompt button
        if (!promptButtonClicked) {
            listener?.onDismiss()
        }
    }

    fun setListener(listener: PromptListener) {
        this.listener = listener
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
                promptButtonClicked = true
                if (survey != null) {
                    listener?.onPromptButtonClick(survey)
                }
                dismiss()
            }
        }
    }

    companion object {
        private const val SURVEY = "survey"

        fun newInstance(survey: Survey): PromptView {
            val bundle = Bundle().apply {
                putParcelable(SURVEY, survey)
            }
            return PromptView().apply {
                arguments = bundle
            }
        }
    }
}
