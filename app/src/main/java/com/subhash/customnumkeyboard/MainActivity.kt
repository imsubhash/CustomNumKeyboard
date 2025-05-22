package com.subhash.customnumkeyboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.subhash.customnumkeyboard.databinding.ActivityMainBinding
import com.subhash.customnumkeyboard.numkeyboard.CustomNumKeyboard

class MainActivity : AppCompatActivity() {

    private lateinit var customKeyboard: CustomNumKeyboard
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customKeyboard = CustomNumKeyboard.Builder(this)
            .withEditTexts(binding.etInput, binding.etInput2)
            .setMaxLength(10)
            .enableHapticFeedback(true)
            .build()

        binding.keyboardContainer.addView(customKeyboard)

    }

    override fun onDestroy() {
        customKeyboard.detachAllEditTexts()
        super.onDestroy()
    }
}