package com.example.easychatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mymoney.R
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.runBlocking

class CategoriesFragment : Fragment() {

    private val conversationHistory = StringBuilder() // Stores the chat history

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eTPrompt = view.findViewById<EditText>(R.id.eTPrompt)
        val btnSubmit = view.findViewById<ImageButton>(R.id.btnSubmit) // Using ImageButton for send icon
        val messageContainer = view.findViewById<LinearLayout>(R.id.messageContainer) // Container for chat messages

        btnSubmit.setOnClickListener {
            val prompt = eTPrompt.text.toString()

            // Ensure the user entered something
            if (prompt.isNotEmpty()) {
                // Append user input to the conversation history
                conversationHistory.append("User: $prompt\n")

                // Create a new TextView for the user message
                val userMessage = TextView(context)
                userMessage.text = "User: $prompt"
                messageContainer.addView(userMessage)

                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = getString(R.string.Gemini_API_Key) // Replace with your API Key
                )

                runBlocking {
                    // Get the response from the model
                    val response = generativeModel.generateContent(conversationHistory.toString())

                    // Add the AI's response to the history
                    conversationHistory.append("${response.text}\n")

                    // Create a new TextView for the AI response
                    val aiMessage = TextView(context)
                    aiMessage.text = "${response.text}"
                    messageContainer.addView(aiMessage)

                    // Scroll to the bottom to show the latest message
                    messageContainer.scrollTo(0, messageContainer.height)
                }

                // Clear EditText after input
                eTPrompt.setText("")
            } else {
                // Show a toast message if the user input is empty
                Toast.makeText(context, "Please type a message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
