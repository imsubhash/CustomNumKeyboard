package com.subhash.customnumkeyboard.numkeyboard


import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import com.subhash.customnumkeyboard.R
import kotlin.math.min

class CustomNumKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val attachedEditTexts = mutableListOf<EditText>()

    private var activeEditText: EditText? = null
    private val deleteHandler = Handler(Looper.getMainLooper())
    private var isDeleting = false
    private val editTextMaxLengthMap = mutableMapOf<EditText, Int>()

    init {
        inflate(context, R.layout.custom_num_keyboard, this)
        val keyboardLayout: GridLayout = findViewById(R.id.gridLayoutKeyboard)
        setupButtonListeners(keyboardLayout)
    }


    class Builder(private val context: Context) {
        private val editTexts = mutableListOf<EditText>()
        private var maxLength: Int = Int.MAX_VALUE
        private var themeResId: Int = 0
        private var hapticFeedbackEnabled: Boolean = true

        fun withEditText(editText: EditText): Builder {
            editTexts.add(editText)
            return this
        }

        fun withEditTexts(vararg editTexts: EditText): Builder {
            this.editTexts.addAll(editTexts)
            return this
        }

        fun setMaxLength(maxLength: Int): Builder {
            this.maxLength = maxLength
            return this
        }

        fun setTheme(@StyleRes themeResId: Int): Builder {
            this.themeResId = themeResId
            return this
        }

        fun enableHapticFeedback(enabled: Boolean): Builder {
            this.hapticFeedbackEnabled = enabled
            return this
        }

        fun build(): CustomNumKeyboard {
            val keyboard = if (themeResId != 0) {
                CustomNumKeyboard(ContextThemeWrapper(context, themeResId))
            } else {
                CustomNumKeyboard(context)
            }

            if (editTexts.isNotEmpty()) {
                keyboard.attachToEditText(*editTexts.toTypedArray(), maxLength = maxLength)
            }
            keyboard.isHapticFeedbackEnabled = hapticFeedbackEnabled
            return keyboard
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun attachToEditText(vararg editTexts: EditText, maxLength: Int = Int.MAX_VALUE) {
        editTexts.forEach { editText ->
            if (!attachedEditTexts.contains(editText)) {
                editText.showSoftInputOnFocus = false
                editTextMaxLengthMap[editText] = maxLength
                attachedEditTexts.add(editText)

                editText.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        activeEditText = v as EditText
                    }
                }

                editText.setOnTouchListener { v, event ->
                    v as EditText
                    if (event.action == MotionEvent.ACTION_UP) {
                        activeEditText = v
                        val offset = v.getOffsetForPosition(event.x, event.y)
                        v.setSelection(offset)
                    }
                    false
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun detachAllEditTexts() {
        attachedEditTexts.forEach { editText ->
            editText.onFocusChangeListener = null
            editText.setOnTouchListener(null)
            editText.showSoftInputOnFocus = true
        }
        attachedEditTexts.clear()
        editTextMaxLengthMap.clear()
        activeEditText = null
    }

    private fun setupButtonListeners(keyboardLayout: GridLayout) {
        for (i in 0 until keyboardLayout.childCount) {
            val keyView = keyboardLayout.getChildAt(i)
            if (keyView is TextView) {
                keyView.setOnClickListener(this)
            }
            if (keyView.id == R.id.crossButton) {
                setDeleteButtonLongPress(keyView)
            }
        }
    }

    override fun onClick(v: View?) {
        if (isHapticFeedbackEnabled) {
            performHapticFeedback(v)
        }
        activeEditText?.apply {
            isCursorVisible = true
            requestFocus()
        }

        if (v is TextView) {
            when (val key = v.text.toString()) {
                context.getString(R.string.key_backspace) -> activeEditText?.removeCharBeforeSelection()
                context.getString(R.string.key_dot) -> {
                    val newChar = key[0]
                    activeEditText?.addCharAfterSelection(newChar)
                }

                else -> {
                    val newChar = key[0]
                    activeEditText?.addCharAfterSelection(newChar)
                }
            }
        }
    }

    private val deleteRunnable = object : Runnable {
        override fun run() {
            if (isDeleting) {
                deleteLastCharacter()
                deleteHandler.postDelayed(this, 50)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setDeleteButtonLongPress(deleteButton: View) {
        deleteButton.setOnLongClickListener {
            isDeleting = true
            deleteHandler.post(deleteRunnable)
            true
        }

        deleteButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDeleting = false
                    deleteHandler.removeCallbacks(deleteRunnable)
                }
            }
            false
        }
    }


    private fun deleteLastCharacter() {
        activeEditText?.apply {

            val text = this.text.toString()
            if (text.isNotEmpty()) {
                if (isHapticFeedbackEnabled) {
                    performHapticFeedback(activeEditText)
                }
                setText(text.substring(0, text.length - 1))
                setSelection(this.text.length)
            }
        }
    }

    private fun performHapticFeedback(v: View?) {
        v?.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    private fun EditText.addCharAfterSelection(newChar: Char) {

        if (isBiggerThanMaxLength(this, text)) {
            return
        }

        val selectionEnd = this.selectionEnd
        val newTextBuilder = StringBuilder()
            .append(text.subSequence(0, selectionEnd))
            .append(newChar)
            .append(text.subSequence(selectionEnd, length()))
        setText(newTextBuilder)
        setSelectionWithValidation(selectionEnd + 1)
    }

    private fun EditText.removeCharBeforeSelection() {
        val removableCharPosition = selectionEnd - 1
        if (removableCharPosition < 0) {
            return
        }

        val newTextBuilder = StringBuilder()
            .append(text.substring(0, removableCharPosition))
            .append(text.substring(removableCharPosition + 1))

        setText(newTextBuilder)
        setSelectionWithValidation(removableCharPosition)
    }

    private fun EditText.setSelectionWithValidation(index: Int) {
        setSelection(min(index, text.length))
    }

    private fun isBiggerThanMaxLength(
        editText: EditText,
        text: CharSequence
    ): Boolean {
        val maxLength = editTextMaxLengthMap[editText] ?: Int.MAX_VALUE
        return maxLength > 0 && text.length >= maxLength
    }

}