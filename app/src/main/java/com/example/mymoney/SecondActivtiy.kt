package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)

        // Set window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.second_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val back_buttom=findViewById<Button>(R.id.Back)
        back_buttom.setOnClickListener {
            val intent_back = Intent(this,MainActivity::class.java)
            startActivity(intent_back)
        }
        val next_buttom=findViewById<Button>(R.id.Next)
        next_buttom.setOnClickListener {
            val intent_next= Intent(this,ThirdActivity::class.java)
            startActivity(intent_next)
        }




    }
}
