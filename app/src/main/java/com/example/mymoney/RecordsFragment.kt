package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymoney.databinding.FragmentRecordsBinding
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

// Data class to represent a record (e.g., Income or Expense)
data class Record(
    val id: Int,
    val type: String, // "Income" or "Expense"
    val account: String, // Added field for account
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
)

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private val recordsList = mutableListOf<Record>()

    private lateinit var tvDate: TextView
    private lateinit var btnPrevDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)

        recyclerView = binding.rvRecords
        recyclerView.layoutManager = LinearLayoutManager(context)
        recordsAdapter = RecordsAdapter(recordsList)
        recyclerView.adapter = recordsAdapter

        tvDate = binding.tvDate
        btnPrevDay = binding.btnPrevDay
        btnNextDay = binding.btnNextDay

        calendar = Calendar.getInstance()
        dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        updateDate()

        btnPrevDay.setOnClickListener {
            calendar.add(Calendar.DATE, -1)
            updateDate()
        }

        btnNextDay.setOnClickListener {
            calendar.add(Calendar.DATE, 1)
            updateDate()
        }

        setupSpinners()
        setupButtons()
        fetchRecordsFromFirebase()

        return binding.root
    }

    private fun updateDate() {
        val formattedDate = dateFormat.format(calendar.time)
        tvDate.text = formattedDate
    }

    private fun setupSpinners() {
        val income_expanse = listOf("Select", "Income", "Expense")
        val income_ExpanseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, income_expanse)
        income_ExpanseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIncomeExpense.adapter = income_ExpanseAdapter

        val accountOptions = listOf("Payment Method", "Cash", "Bank", "Credit Card")
        val accountAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, accountOptions)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAccount.adapter = accountAdapter

        val categoryOptions = listOf("Category", "Food", "Transport", "Shopping", "Salary", "Freelance")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryOptions)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnConfirm.setOnClickListener {
            val type = binding.spinnerIncomeExpense.selectedItem.toString()
            val date = binding.tvDate.text.toString()
            val description = binding.editTextNotes.text.toString()
            val account = binding.spinnerAccount.selectedItem.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val amountText = binding.editTextAmount.text.toString().trim()  // Correct reference to the EditText
            val cleanedAmount = amountText.replace("[^0-9.]".toRegex(), "") // Remove any non-numeric characters except for the decimal point

// Now parse the cleaned amount
            val amount = if (cleanedAmount.isNotEmpty()) {
                try {
                    cleanedAmount.toDouble()
                } catch (e: NumberFormatException) {
                    0.0 // Default value if parsing fails
                }
            } else {
                0.0 // Default value if empty
            }


            val record = Record(
                id = System.currentTimeMillis().toInt(),
                type = type,
                amount = amount,
                date = date,
                description = description,
                account = account,
                category = category
            )

            val database = FirebaseDatabase.getInstance()
            val recordsRef = database.getReference("records")

            recordsRef.push().setValue(record)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Record added", Toast.LENGTH_SHORT).show()
                        fetchRecordsFromFirebase() // Fetch updated records after adding a new one
                    } else {
                        Toast.makeText(context, "Failed to add record", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fetchRecordsFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val recordsRef = database.getReference("records")

        recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recordsList.clear()

                for (recordSnapshot in snapshot.children) {
                    try {
                        // Log raw data for debugging purposes
                        Log.d("FirebaseRecord", "Snapshot Value: ${recordSnapshot.value}")

                        val id = recordSnapshot.child("id").getValue(Int::class.java) ?: 0
                        val type = recordSnapshot.child("type").getValue(String::class.java) ?: "Unknown"
                        val amountText = binding.editTextAmount.text.toString().trim()  // Correct reference to the EditText

                        // Clean the input by removing any non-numeric characters except for the decimal point
                        val cleanedAmount = amountText.replace("[^0-9.]".toRegex(), "")

                        // Parse the cleaned amount
                        val amount = if (cleanedAmount.isNotEmpty()) {
                            try {
                                cleanedAmount.toDouble()
                            } catch (e: NumberFormatException) {
                                0.0 // Default value if parsing fails
                            }
                        } else {
                            0.0 // Default value if empty
                        }



                        val date = recordSnapshot.child("date").getValue(String::class.java) ?: "Unknown"
                        val description = recordSnapshot.child("description").getValue(String::class.java) ?: "No Description"
                        val account = recordSnapshot.child("account").getValue(String::class.java) ?: "Unknown"
                        val category = recordSnapshot.child("category").getValue(String::class.java) ?: "Unknown"

                        Log.d("FirebaseRecord", "Parsed Record - Amount: $amount, ID: $id, Type: $type")

                        val record = Record(
                            id = id,
                            type = type,
                            amount = amount, // Convert to string
                            date = date,
                            description = description,
                            account = account,
                            category = category
                        )

                        recordsList.add(record)
                    } catch (e: Exception) {
                        Log.e("FirebaseRecord", "Error parsing record", e)
                    }
                }

                recordsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load records", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class RecordsAdapter(private val records: List<Record>) : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.bind(record)
    }

    override fun getItemCount() = records.size

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvType: TextView = itemView.findViewById(R.id.tv_record_type)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_record_amount)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_record_date)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_record_description)

        fun bind(record: Record) {
            tvType.text = record.type
            tvAmount.text = "$${record.amount}"
            tvDate.text = record.date
            tvDescription.text = record.description
        }
    }
}
