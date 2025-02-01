package com.example.mymoney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FirstActivity : AppCompatActivity() {
    private var selectedCurrency: String? = null
    private var selectedTextView: TextView? = null
    private val currencySymbols = mapOf(
        "Afghan Afghani - AFN" to "؋",
        "Albanian Lek - ALL" to "L",
        "Algerian Dinar - DZD" to "د.ج",
        "Andorran Peseta - ADP" to "₧",
        "Angolan Kwanza - AOA" to "Kz",
        "East Caribbean Dollar - XCD" to "$",
        "Argentine Peso - ARS" to "$",
        "Armenian Dram - AMD" to "֏",
        "Aruban Florin - AWG" to "ƒ",
        "Australian Dollar - AUD" to "$",
        "Azerbaijani Manat - AZN" to "₼",
        "Bahamian Dollar - BSD" to "$",
        "Bahraini Dinar - BHD" to ".د.ب",
        "Bangladeshi Taka - BDT" to "৳",
        "Barbadian Dollar - BBD" to "$",
        "Belarusian Ruble - BYN" to "Br",
        "Belgian Franc - BEF" to "₣",
        "Belize Dollar - BZD" to "$",
        "Bermudian Dollar - BMD" to "$",
        "Bhutanese Ngultrum - BTN" to "Nu.",
        "Bolivian Boliviano - BOB" to "Bs.",
        "Bosnia and Herzegovina Convertible Mark - BAM" to "KM",
        "Botswana Pula - BWP" to "P",
        "Brazilian Real - BRL" to "R$",
        "British Pound Sterling - GBP" to "£",
        "Brunei Dollar - BND" to "$",
        "Bulgarian Lev - BGN" to "лв",
        "Burundian Franc - BIF" to "₣",
        "Cambodian Riel - KHR" to "៛",
        "Cameroonian Franc - CFA" to "₣",
        "Canadian Dollar - CAD" to "$",
        "Cape Verdean Escudo - CVE" to "$",
        "Cayman Islands Dollar - KYD" to "$",
        "Central African CFA Franc - XAF" to "₣",
        "Chilean Peso - CLP" to "$",
        "Chinese Yuan - CNY" to "¥",
        "Colombian Peso - COP" to "$",
        "Comorian Franc - KMF" to "₣",
        "Congolese Franc - CDF" to "₣",
        "Costa Rican Colón - CRC" to "₡",
        "Croatian Kuna - HRK" to "kn",
        "Cuban Peso - CUP" to "$",
        "Czech Koruna - CZK" to "Kč",
        "Danish Krone - DKK" to "kr",
        "Djiboutian Franc - DJF" to "₣",
        "Dominican Peso - DOP" to "$",
        "Egyptian Pound - EGP" to "£",
        "Eritrean Nakfa - ERN" to "Nfk",
        "Estonian Kroon - EEK" to "kr",
        "Ethiopian Birr - ETB" to "ብር",
        "Euro - EUR" to "€",
        "Falkland Islands Pound - FKP" to "£",
        "Fijian Dollar - FJD" to "$",
        "Gambian Dalasi - GMD" to "D",
        "Georgian Lari - GEL" to "₾",
        "Ghanaian Cedi - GHS" to "₵",
        "Gibraltar Pound - GIP" to "£",
        "Guatemalan Quetzal - GTQ" to "Q",
        "Guinean Franc - GNF" to "₣",
        "Guyanese Dollar - GYD" to "$",
        "Haitian Gourde - HTG" to "G",
        "Honduran Lempira - HNL" to "L",
        "Hong Kong Dollar - HKD" to "$",
        "Hungarian Forint - HUF" to "Ft",
        "Icelandic Króna - ISK" to "kr",
        "Indian Rupee - INR" to "₹",
        "Indonesian Rupiah - IDR" to "Rp",
        "Iranian Rial - IRR" to "﷼",
        "Iraqi Dinar - IQD" to "ع.د",
        "Israeli New Shekel - ILS" to "₪",
        "Jamaican Dollar - JMD" to "$",
        "Japanese Yen - JPY" to "¥",
        "Jordanian Dinar - JOD" to "د.ا",
        "Kazakhstani Tenge - KZT" to "₸",
        "Kenyan Shilling - KES" to "Sh",
        "Kuwaiti Dinar - KWD" to "د.ك",
        "Kyrgyzstani Som - KGS" to "с",
        "Laotian Kip - LAK" to "₭",
        "Latvian Lats - LVL" to "Ls",
        "Lebanese Pound - LBP" to "ل.ل",
        "Lesotho Loti - LSL" to "L",
        "Liberian Dollar - LRD" to "$",
        "Libyan Dinar - LYD" to "ل.د",
        "Lithuanian Litas - LTL" to "Lt",
        "Luxembourg Franc - LUF" to "₣",
        "Macanese Pataca - MOP" to "P",
        "Macedonian Denar - MKD" to "ден",
        "Malagasy Ariary - MGA" to "Ar",
        "Malawian Kwacha - MWK" to "MK",
        "Malaysian Ringgit - MYR" to "RM",
        "Maldivian Rufiyaa - MVR" to "Rf",
        "Mali CFA Franc - XOF" to "₣",
        "Mauritanian Ouguiya - MRU" to "UM",
        "Mauritian Rupee - MUR" to "₨",
        "Mexican Peso - MXN" to "$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.firstactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.first)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listItemContainer = findViewById<LinearLayout>(R.id.list_item)
        val listNames = currencySymbols.keys.toList()

        for (name: String in listNames) {
            val textView = TextView(this)
            textView.text = name
            textView.textSize = 16f
            textView.setPadding(10, 10, 10, 10)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 10, 10, 10)
            textView.layoutParams = params
            textView.setOnClickListener {
                selectedCurrency = name
                selectedTextView?.setBackgroundColor(Color.TRANSPARENT)
                selectedTextView = textView
                textView.setBackgroundColor(Color.LTGRAY)

                val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("selectedCurrency", name)
                editor.putString("currencySymbol", currencySymbols[name] ?: "Unknown")
                editor.apply()

                Toast.makeText(this, "Selected: $name", Toast.LENGTH_SHORT).show()
            }
            listItemContainer.addView(textView)
        }

        val next = findViewById<Button>(R.id.next)
        next.setOnClickListener {
            if (selectedCurrency != null) {
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a currency before proceeding.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
