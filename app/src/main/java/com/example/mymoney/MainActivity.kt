package com.example.mymoney

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance()

        // Initialize Google Sign-In
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Web client ID from Firebase Console
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Handle system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        val signUpButton: Button = findViewById(R.id.button_sign_in)
        val haveAccount: TextView = findViewById(R.id.already_have_account_text)
        val googleSignInButton: Button = findViewById(R.id.button_continue_with_google)

        // Sign-up button click listener
        signUpButton.setOnClickListener {
            val nameEditText = findViewById<EditText>(R.id.editTextName)
            val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
            val passwordEditText = findViewById<EditText>(R.id.editTextTextPassword)

            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Error icons
            val nameErrorIcon = findViewById<ImageView>(R.id.error_icon_name)
            val emailErrorIcon = findViewById<ImageView>(R.id.error_icon_email)
            val passwordErrorIcon = findViewById<ImageView>(R.id.error_icon_password)

            var isValid = true

            // Reset the error states
            resetErrorState(nameEditText, emailEditText, passwordEditText)
            resetErrorIcon(nameErrorIcon, emailErrorIcon, passwordErrorIcon)
            val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
            val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()

            when {
                name.isEmpty() || name.length < 5 -> {
                    isValid = false
                    showError(nameEditText.context, nameEditText, nameErrorIcon)
                    Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() || !email.matches(emailRegex) -> {
                    isValid = false
                    showError(emailEditText.context, emailEditText, emailErrorIcon)
                    Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() || !password.matches(passwordRegex) -> {
                    isValid = false
                    showError(passwordEditText.context, passwordEditText, passwordErrorIcon)
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                }
            }

            if (isValid) {
                val user = User(name, email, password)
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("users")

                val userId = myRef.push().key
                if (userId != null) {
                    myRef.child(userId).setValue(user).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "User data saved successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.putExtra("name", name)
                            intent.putExtra("email", email)
                            intent.putExtra("password", password)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // Already have an account listener
        haveAccount.setOnClickListener {
            Toast.makeText(this, "Navigating to Login Page", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Google Sign-In button click listener
        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Handle Google Sign-In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Firebase using Google account
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = mAuth.currentUser
                    Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, FirstActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Reset error states
    private fun resetErrorState(vararg editTexts: EditText) {
        for (editText in editTexts) {
            editText.background = ContextCompat.getDrawable(editText.context, R.drawable.normal_border)
        }
    }

    // Reset error icon visibility
    private fun resetErrorIcon(vararg icons: ImageView) {
        for (icon in icons) {
            icon.visibility = View.GONE
        }
    }

    // Show error
    private fun showError(context: Context, editText: EditText, icon: ImageView) {
        val errorDrawable = GradientDrawable()
        errorDrawable.setColor(ContextCompat.getColor(context, R.color.red))
        errorDrawable.cornerRadius = 8f
        editText.background = errorDrawable
        icon.visibility = View.VISIBLE
    }

    // User data class
    data class User(
        val name: String? = "",
        val email: String? = "",
        val password: String? = ""
    )
}
