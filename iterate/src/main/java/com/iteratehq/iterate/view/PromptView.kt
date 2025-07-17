package com.iteratehq.iterate.view

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iteratehq.iterate.Iterate
import com.iteratehq.iterate.R
import com.iteratehq.iterate.databinding.PromptViewBinding
import com.iteratehq.iterate.model.InteractionEventSource
import com.iteratehq.iterate.model.ProgressEventMessageData
import com.iteratehq.iterate.model.Survey
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.image.ImagesPlugin

class PromptView : BottomSheetDialogFragment() {
    interface PromptListener {
        fun onDismiss(
            source: InteractionEventSource,
            progress: ProgressEventMessageData?,
        )

        fun onPromptButtonClick(survey: Survey)
    }

    private lateinit var binding: PromptViewBinding
    private var listener: PromptListener? = null
    private var promptButtonClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
        val defaultColor = "#7457be"
        val survey = arguments?.getParcelable<Survey>(SURVEY)
        val surveyTextFont = arguments?.getString(SURVEY_TEXT_FONT)
        val buttonFont = arguments?.getString(BUTTON_FONT)
        val markwon =
            Markwon
                .builder(requireContext())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(
                    object : AbstractMarkwonPlugin() {
                        override fun configureTheme(builder: MarkwonTheme.Builder) {
                            super.configureTheme(builder)
                            builder.linkColor(Color.parseColor(survey?.color ?: defaultColor))
                        }
                    },
                ).build()

        if (survey == null) {
            dismiss()
            return
        }

        with(binding) {
            btnClose.setOnClickListener {
                dismiss()
            }

            val promptMessage = Iterate.getTranslationForKey("survey.prompt.text", survey) ?: survey.prompt?.message
            markwon.setMarkdown(txtPrompt, promptMessage ?: "")
            if (surveyTextFont != null) {
                txtPrompt.setTypeface(
                    Typeface.createFromAsset(
                        requireContext().assets,
                        surveyTextFont,
                    ),
                )
            }

            // View background & text colors
            if (isDarkTheme()) {
                root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blackLight))
                txtPrompt.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                txtPrompt.setTextColor(ContextCompat.getColor(requireContext(), R.color.blackText))
            }

            // Button background & text colors
            val color = survey?.color ?: defaultColor
            val backgroundColor =
                if (isDarkTheme() && survey?.colorDark != null) survey.colorDark else color

            val buttonText = Iterate.getTranslationForKey("survey.prompt.buttonText", survey) ?: survey.prompt?.buttonText
            btnPrompt.text = buttonText

            if (buttonFont != null) {
                btnPrompt.setTypeface(Typeface.createFromAsset(requireContext().assets, buttonFont))
            }
            btnPrompt.backgroundTintList = ColorStateList.valueOf(Color.parseColor(backgroundColor))
            // Calculate the luminance of the background color and set the text color
            val textColor = if (ColorUtils.calculateLuminance(Color.parseColor(backgroundColor)) < 0.5) Color.WHITE else Color.BLACK
            btnPrompt.setTextColor(textColor)

            survey.borderRadius?.let { radius ->
                val radiusValue = radius.replace("px", "").toFloat()
                btnPrompt.cornerRadius = radiusValue.toInt()
            }

            btnPrompt.setOnClickListener {
                promptButtonClicked = true
                listener?.onPromptButtonClick(survey)
                dismiss()
            }
        }
    }

    private fun isDarkTheme(): Boolean {
        val survey = arguments?.getParcelable<Survey>(SURVEY)
        if (survey?.appearance == "dark") return true
        if (survey?.appearance == "light") return false
        return (resources.configuration.uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES
    }

    companion object {
        private const val SURVEY = "survey"
        private const val SURVEY_TEXT_FONT = "survey_text_font"
        private const val BUTTON_FONT = "button_font"

        fun newInstance(
            survey: Survey,
            surveyTextFont: String? = null,
            buttonFont: String? = null,
        ): PromptView {
            val bundle =
                Bundle().apply {
                    putParcelable(SURVEY, survey)
                    putString(SURVEY_TEXT_FONT, surveyTextFont)
                    putString(BUTTON_FONT, buttonFont)
                }
            return PromptView().apply {
                arguments = bundle
            }
        }
    }
}
