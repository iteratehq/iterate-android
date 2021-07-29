package com.iteratehq.iterate.view

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iteratehq.iterate.R
import com.iteratehq.iterate.databinding.PromptViewBinding
import com.iteratehq.iterate.model.InteractionEventSource
import com.iteratehq.iterate.model.ProgressEventMessageData
import com.iteratehq.iterate.model.Survey

class PromptView : BottomSheetDialogFragment() {

    interface PromptListener {
        fun onDismiss(source: InteractionEventSource, progress: ProgressEventMessageData?)
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
        // Create ContextThemeWrapper with the custom theme
        val contextThemeWrapper =
            ContextThemeWrapper(requireContext(), R.style.Theme_IterateLibrary)
        // Clone the inflater using the ContextThemeWrapper
        val clonedInflater = inflater.cloneInContext(contextThemeWrapper)
        // Inflate the layout using the cloned inflater, not the default inflater
        binding = PromptViewBinding.inflate(clonedInflater)
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
            listener?.onDismiss(InteractionEventSource.PROMPT, null)
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

            val color = survey?.color ?: "#7457be"
            val backgroundColor =
                if (isDarkTheme() && survey?.colorDark != null) survey.colorDark else color
            btnPrompt.text = survey?.prompt?.buttonText
            btnPrompt.backgroundTintList = ColorStateList.valueOf(Color.parseColor(backgroundColor))
            btnPrompt.setOnClickListener {
                promptButtonClicked = true
                if (survey != null) {
                    listener?.onPromptButtonClick(survey)
                }
                dismiss()
            }
        }
    }

    private fun isDarkTheme(): Boolean {
        return (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
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
