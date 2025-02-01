package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThirdActivity  : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.third_activity)

            // Set window insets listener
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.third_activity)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
            val back_button=findViewById<Button>(R.id.Back)
            back_button.setOnClickListener {
                val intent= Intent(this,SecondActivity::class.java)
                startActivity(intent)
            }
            val radioButton=findViewById<RadioButton>(R.id.radioButton)
            val next_button=findViewById<Button>(R.id.Next)
            next_button.setOnClickListener {
                if (radioButton.isChecked) {
                    // If the RadioButton is selected, navigate to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    // If the RadioButton is not selected, show a message
                    Toast.makeText(this, "Please select the option to proceed", Toast.LENGTH_SHORT).show()
                }
                }
        }
}
