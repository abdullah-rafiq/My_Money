package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.log_in)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Create account navigation
        val createaccount: TextView = findViewById(R.id.create_account_log_in_to_sign_in)
        createaccount.setOnClickListener {
            Toast.makeText(this, "Navigating to Sign up Screen", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Initialize UI elements
        val emailText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordText = findViewById<EditText>(R.id.editTextTextPassword)
        val loginButton: Button = findViewById(R.id.button_login)

        loginButton.setOnClickListener {
            val enteredEmail = emailText.text.toString().trim()
            val enteredPassword = passwordText.text.toString().trim()

            // Check if email and password are entered
            if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Fetch user data from Firebase and validate login
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("users")

                // Query the users node to find the user with the entered email
                usersRef.orderByChild("email").equalTo(enteredEmail).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // User exists
                            for (userSnapshot in snapshot.children) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user != null && user.password == enteredPassword) {
                                    // Password matched, navigate to profile
                                    Toast.makeText(this@LoginActivity, "Password matched! Navigating to Profile.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LoginActivity, FirstActivity::class.java)
                                    startActivity(intent)
                                    return
                                }
                            }
                            // If password doesn't match
                            Toast.makeText(this@LoginActivity, "Password doesn't match!", Toast.LENGTH_SHORT).show()
                        } else {
                            // If email doesn't exist
                            Toast.makeText(this@LoginActivity, "No user found with this email.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Failed to read data from Firebase.", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    // Data class to represent User
    data class User(
        val name: String? = "",
        val email: String? = "",
        val password: String? = ""
    )
}
