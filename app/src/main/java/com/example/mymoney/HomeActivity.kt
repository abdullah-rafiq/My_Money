package com.example.mymoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var btnPrevDay: ImageButton
    private lateinit var btnNextDay: ImageButton
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var toolbar: Toolbar
    private lateinit var calendarHeader: LinearLayout
    private lateinit var expenseIncomeRow: LinearLayout
    private lateinit var amountRow: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var floatingButton: FloatingActionButton
    lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private lateinit var incomeTextView: TextView
    private lateinit var expenseTextView: TextView
    private lateinit var totalTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // Handle system bar insets (for modern Android devices)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        incomeTextView = findViewById(R.id.incomeAmount)
        expenseTextView = findViewById(R.id.expenseAmount)
        totalTextView = findViewById(R.id.totalAmount)

        // Fetch and update financial summary
        fetchAndUpdateFinanceSummary()
        // Initialize drawer layout and toolbar
        drawerLayout = findViewById(R.id.home_activity)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up the drawer toggle for opening the navigation drawer
        val drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // Initialize NavigationView
        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_analyse -> loadFragment(AnalyseFragment())
                R.id.nav_budget -> loadFragment(BudgetFragment())
                R.id.nav_accounts -> loadFragment(AccountsFragment())
                R.id.nav_categories -> loadFragment(CategoriesFragment())
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Initialize UI components
        calendarHeader = findViewById(R.id.calendar_header)
        expenseIncomeRow = findViewById(R.id.view)
        amountRow = findViewById(R.id.linearLayout2)
        bottomNav = findViewById(R.id.bottom_navigation)
//        spinner = findViewById(R.id.spinner)
        floatingButton = findViewById(R.id.floatingActionButton)

        // Initialize the calendar and date format
        tvDate = findViewById(R.id.tv_date)
        btnPrevDay = findViewById(R.id.btn_prev_day)
        btnNextDay = findViewById(R.id.btn_next_day)

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

        // FloatingActionButton click to open a fragment in fullscreen
        floatingButton.setOnClickListener {
            Log.d("FragmentTest", "FloatingActionButton clicked, showing AddExpenseFragment.")

            hideHomeUI()

            val fragment = RecordsFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
        }

        // BottomNavigationView handling
        bottomNav.setOnItemSelectedListener { menuItem ->
            hideHomeUI()
            if (menuItem.itemId == R.id.nav_records) {
                Log.d("FragmentTest", "nav_records clicked, closing current fragment and opening HomeActivity.")

                // Close the current fragment and navigate to HomeActivity
                val transaction = supportFragmentManager.beginTransaction()

                // You can either replace the fragment with a dummy fragment or remove the current fragment
                transaction.replace(R.id.fragment_container, Fragment())  // Replace with an empty fragment
                transaction.commitNow()  // Use commitNow() to ensure the transaction completes immediately

                // Now, navigate to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()  // Close the current activity

                return@setOnItemSelectedListener true  // Prevent further fragment loading
            }
            val selectedFragment: Fragment = when (menuItem.itemId) {
                R.id.nav_analyse -> AnalyseFragment()
                R.id.nav_budget -> BudgetFragment()
                R.id.nav_accounts -> AccountsFragment()
                R.id.nav_categories -> CategoriesFragment()
                else -> throw IllegalArgumentException("Unknown menu item")
            }

            Log.d("FragmentTest", "BottomNavigationView item clicked, loading fragment.")
            val loadFragment=selectedFragment
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, loadFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
            true
        }

    }

    private fun updateDate() {
        val formattedDate = dateFormat.format(calendar.time)
        tvDate.text = formattedDate
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)  // Optional, if you want back navigation
//        transaction.commit()
        transaction.commitNow()
    }


    private fun hideHomeUI() {
        toolbar.visibility = View.GONE
        calendarHeader.visibility = View.GONE
        expenseIncomeRow.visibility = View.GONE
        amountRow.visibility = View.GONE
        bottomNav.visibility = View.GONE
        floatingButton.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

        // Show UI elements again when returning to home
        toolbar.visibility = View.VISIBLE
        calendarHeader.visibility = View.VISIBLE
        expenseIncomeRow.visibility = View.VISIBLE
        amountRow.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
        floatingButton.visibility = View.VISIBLE
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
                        Log.e("HomeActivity", "Error parsing amount: ${e.message}")
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

                // Update UI safely on the main thread
                runOnUiThread {
                    incomeTextView.post { incomeTextView.text = String.format("%.2f", totalIncome) }
                    expenseTextView.post { expenseTextView.text = String.format("%.2f", totalExpense) }
                    totalTextView.post { totalTextView.text = String.format("%.2f", totalAmount) }
                }

                Log.d("HomeActivity", "Updated Finance Summary - Income: $totalIncome, Expense: $totalExpense, Total: $totalAmount")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Error fetching finance summary", error.toException())
            }
        })
    }

}