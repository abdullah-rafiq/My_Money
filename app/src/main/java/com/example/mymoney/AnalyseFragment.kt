package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalyseFragment : Fragment() {
    private lateinit var pieChart: com.github.mikephil.charting.charts.PieChart
    private lateinit var btnPrevDay: ImageButton
    private lateinit var tvDate: TextView
    private lateinit var btnNextDay: ImageButton
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var toolbar: Toolbar
    private lateinit var calendarHeader: LinearLayout
    private lateinit var expenseIncomeRow: LinearLayout
    private lateinit var amountRow: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var spinner: Spinner
    private lateinit var floatingButton: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analyse, container, false)

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View, insets: WindowInsetsCompat ->
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            )
            insets
        }
        pieChart = view.findViewById(R.id.pieChart)
        fetchDataFromFirebase()

        // Initialize UI components
        drawerLayout = view.findViewById(R.id.fragment_analyse)
        toolbar = view.findViewById(R.id.toolbar)
        calendarHeader = view.findViewById(R.id.calendar_header)
        expenseIncomeRow = view.findViewById(R.id.view)
        amountRow = view.findViewById(R.id.linearLayout2)
        bottomNav = view.findViewById(R.id.bottom_navigation)
        spinner = view.findViewById(R.id.spinner)
        floatingButton = view.findViewById(R.id.floatingActionButton)
        navigationView = view.findViewById(R.id.navigation_view)

        // Setup Drawer Toggle
        val drawerToggle = ActionBarDrawerToggle(
            activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Navigation View Handling
        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_budget -> loadFragment(BudgetFragment())
                R.id.nav_accounts -> loadFragment(AccountsFragment())
                R.id.nav_categories -> loadFragment(CategoriesFragment())
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()  // Close the current activity
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        })

        // Initialize Calendar Controls
        tvDate = view.findViewById(R.id.tv_date)
        btnPrevDay = view.findViewById(R.id.btn_prev_day)
        btnNextDay = view.findViewById(R.id.btn_next_day)

        calendar = Calendar.getInstance()
        dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        updateDate()

        btnPrevDay.setOnClickListener(View.OnClickListener { v: View? ->
            calendar.add(Calendar.DATE, -1)
            updateDate()
        })

        btnNextDay.setOnClickListener(View.OnClickListener { v: View? ->
            calendar.add(Calendar.DATE, 1)
            updateDate()
        })

        // Floating Action Button Handling
        floatingButton.setOnClickListener(View.OnClickListener { v: View? ->
            Log.d(
                "AnalyseFragment",
                "FloatingActionButton clicked, launching RecordsFragment."
            )
            hideUI()
            loadFragment(RecordsFragment())
        })

        // BottomNavigationView Handling
        bottomNav.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { menuItem: MenuItem ->
            hideUI()
            val selectedFragment = when (menuItem.itemId) {
                R.id.nav_budget -> BudgetFragment()
                R.id.nav_accounts -> AccountsFragment()
                R.id.nav_categories -> CategoriesFragment()
                else -> false
            }
            loadFragment(selectedFragment)
            true
        })

        return view
    }

    private fun updateDate() {
        tvDate.text = dateFormat.format(calendar.time)
    }

    private fun loadFragment(fragment: Any) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment as Fragment)
            .addToBackStack(null)
            .commit()
    }


    private fun hideUI() {
        toolbar.visibility = View.GONE
        calendarHeader.visibility = View.GONE
        expenseIncomeRow.visibility = View.GONE
        amountRow.visibility = View.GONE
        bottomNav.visibility = View.GONE
        spinner.visibility = View.GONE
        floatingButton.visibility = View.GONE
    }


    private fun fetchDataFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val recordsRef = database.getReference("records")

        recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryMap = mutableMapOf<String, Float>()

                for (recordSnapshot in snapshot.children) {
                    try {
                        // Log raw data for debugging purposes
                        Log.d("FirebaseRecord", "Snapshot Value: ${recordSnapshot.value}")

                        val category = recordSnapshot.child("category").getValue(String::class.java) ?: "Unknown"
                        val amountValue = recordSnapshot.child("amount").value

                        // Handle different possible data formats
                        val amount = when (amountValue) {
                            is Number -> amountValue.toFloat()
                            is String -> amountValue.toFloatOrNull() ?: 0f
                            else -> 0f
                        }

                        Log.d("FirebaseRecord", "Parsed Record - Category: $category, Amount: $amount")

                        categoryMap[category] = categoryMap.getOrDefault(category, 0f) + amount
                    } catch (e: Exception) {
                        Log.e("FirebaseRecord", "Error parsing record", e)
                    }
                }

                Log.d("FirebaseRecord", "Final Category Map: $categoryMap")

                updatePieChart(categoryMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load records", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseRecord", "Error fetching data", error.toException())
            }
        })
    }

    private fun updatePieChart(categoryMap: Map<String, Float>) {
        if (!::pieChart.isInitialized) {
            Log.e("AnalyseFragment", "PieChart is not initialized!")
            return
        }

        if (categoryMap.isEmpty()) {
            Log.w("AnalyseFragment", "No data to display in PieChart")
            return
        }

        val entries = categoryMap.map { (category, amount) ->
            PieEntry(amount, category)
        }

        val dataSet = PieDataSet(entries, "Expense Breakdown")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            animateY(1000)
            invalidate()
        }

        Log.d("AnalyseFragment", "PieChart updated with ${entries.size} entries")
    }

    private fun fetchAndUpdateFinanceSummary() {
        val database = FirebaseDatabase.getInstance()
        val recordsRef = database.getReference("records")

        recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalIncome = 0.0
                var totalExpense = 0.0

                for (recordSnapshot in snapshot.children) {
                    val type = recordSnapshot.child("type").getValue(String::class.java) ?: ""

                    // Handle amount safely as both Double & String
                    val amount: Double = try {
                        when (val amountValue = recordSnapshot.child("amount").value) {
                            is Long -> amountValue.toDouble() // If stored as Long
                            is Double -> amountValue // If stored as Double
                            is String -> amountValue.toDoubleOrNull() ?: 0.0 // If stored as String
                            else -> 0.0 // Default case
                        }
                    } catch (e: Exception) {
                        Log.e("AnalyseFragment", "Error parsing amount: ${e.message}")
                        0.0
                    }

                    // Categorize as Income or Expense
                    if (type.equals("Income", ignoreCase = true)) {
                        totalIncome += amount
                    } else if (type.equals("Expense", ignoreCase = true)) {
                        totalExpense += amount
                    }
                }

                val totalAmount = totalIncome - totalExpense

                // Update UI safely in Fragment
                view?.findViewById<TextView>(R.id.incomeAmount)?.text = String.format("%.2f", totalIncome)
                view?.findViewById<TextView>(R.id.expenseAmount)?.text = String.format("%.2f", totalExpense)
                view?.findViewById<TextView>(R.id.totalAmount)?.text = String.format("%.2f", totalAmount)

                Log.d("AnalyseFragment", "Updated Finance Summary - Income: $totalIncome, Expense: $totalExpense, Total: $totalAmount")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnalyseFragment", "Error fetching finance summary", error.toException())
            }
        })
    }

    //bar chart

    private fun fetchDataForBarChart() {
        val database = FirebaseDatabase.getInstance()
        val recordsRef = database.getReference("records")

        recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryMap = mutableMapOf<String, Float>()

                for (recordSnapshot in snapshot.children) {
                    val category = recordSnapshot.child("category").getValue(String::class.java) ?: "Unknown"
                    val amount = when (val amountValue = recordSnapshot.child("amount").value) {
                        is Number -> amountValue.toFloat()
                        is String -> amountValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }

                    categoryMap[category] = categoryMap.getOrDefault(category, 0f) + amount
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load records", Toast.LENGTH_SHORT).show()
                Log.e("FirebaseRecord", "Error fetching data", error.toException())
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch data after view is fully created


        fetchAndUpdateFinanceSummary()
    }
}
