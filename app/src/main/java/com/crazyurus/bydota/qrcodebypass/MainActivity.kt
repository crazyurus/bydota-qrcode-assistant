package com.crazyurus.bydota.qrcodebypass

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Debug
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.btet.automap.R
import com.byd.library.SaveReadManager

class MainActivity: AppCompatActivity() {
    private val filePath = "/collect2/cloudservice/"
    private val fileName = "salesMark.dat"
    private val salesMark = "di_cloud_sales_m"
    private val countryCodePackageName = "com.byd.countrycodetool"
    private var isSaveReadManagerConnected = false
    private val whiteListCountry = arrayOf(
        "856",
        "43",
        "32",
        "359",
        "357",
        "385",
        "420",
        "45",
        "372",
        "358",
        "33",
        "49",
        "30",
        "36",
        "353",
        "39",
        "371",
        "370",
        "352",
        "356",
        "31",
        "48",
        "351",
        "40",
        "421",
        "386",
        "34",
        "46",
        "354",
        "47",
        "41",
        "423",
        "377",
        "379",
        "44",
        "972",
        "90",
        "81",
        "82"
    )

    private fun getDiLinkVersion(): String {
        if (Debug.isDebuggerConnected()) {
            return "13.1.22.2304120.1"
        }

        val cls = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("get", String::class.java, String::class.java)
        val version = method.invoke(null, "apps.setting.product.outswver", "")?.toString()

        if (version != null) {
            return version
        }

        return ""
    }

    private fun getCountryCode(): String {
        if (Debug.isDebuggerConnected()) {
            return "90"
        }

        val cls = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("get", String::class.java, String::class.java)
        val countryCode = method.invoke(null, "sys.byd.countrycode", "")?.toString()

        if (countryCode != null) {
            return countryCode
        }

        return ""
    }

    private fun getIMEI(): String {
        if (Debug.isDebuggerConnected()) {
            return "12345678"
        }

        val cls = Class.forName("android.os.SystemProperties")
        val method = cls.getMethod("get", String::class.java, String::class.java)
        val iemi = method.invoke(null, "ril.imei", "")?.toString()

        if (iemi != null) {
            return iemi
        }

        return ""
    }

    private fun checkStatus() {
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        if (SaveReadManager.getInstance().readFile(filePath + fileName) == salesMark) {
            button.isEnabled = true
            button2.isEnabled = false
        } else {
            button.isEnabled = false
            button2.isEnabled = true

            Toast.makeText(this, getString(R.string.result_already), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isCountryCodeToolInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(countryCodePackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val version = getDiLinkVersion()
        val countryCode = getCountryCode()
        val imei = getIMEI()
        val isNeedSkip = whiteListCountry.contains(countryCode);
        val textView3 = findViewById<TextView>(R.id.textView3)
        val textView4 = findViewById<TextView>(R.id.textView4)
        val textCountryCode = findViewById<TextView>(R.id.textCountryCode)
        val textVersion = findViewById<TextView>(R.id.textVersion)
        val textLink = findViewById<TextView>(R.id.textLink)
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)

        if (version.isEmpty()) {
            Toast.makeText(this, getString(R.string.text_mobile), Toast.LENGTH_SHORT).show()

            finish()
        } else {
            textView3.text = getString(R.string.status_not_support, version)
        }

        if (countryCode.isEmpty()) {
            textCountryCode.visibility = View.GONE
            button3.visibility = View.GONE
        } else {
            val introduction = if (isNeedSkip) getString(R.string.text_country_code_need) else getString((R.string.text_country_code_not))

            textCountryCode.text = getString(R.string.text_country_code, countryCode, introduction)

            if (!isNeedSkip) {
                textView3.text = getString(R.string.status_support_normal, version)
            }
        }

        if (imei.isNotEmpty()) {
            textLink.text =  "https://bydota.crazyurus.com/code?imei=" + imei.substring(imei.length - 6)
        }

        textVersion.text = getString(R.string.bottom_version, packageManager.getPackageInfo(packageName, 0).versionName)
        SaveReadManager.getInstance().init(this, object : SaveReadManager.IServiceConnectListener {
            override fun onBinderDied() {}

            override fun onServiceConnected() {
                checkStatus()

                isSaveReadManagerConnected = true
                textView3.text = getString(R.string.status_support, version)
                textView4.visibility = View.VISIBLE
                textCountryCode.visibility = View.GONE
            }

            override fun onServiceDisconnected() {
                isSaveReadManagerConnected = false
            }
        })

        button.setOnClickListener {
            if (isSaveReadManagerConnected) {
                SaveReadManager.getInstance().saveFile(filePath, fileName, "crazyurus")
                button.isEnabled = false
                button2.isEnabled = true

                Toast.makeText(this, getString(R.string.result_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.result_fail), Toast.LENGTH_SHORT).show()
            }
        }

        button2.setOnClickListener {
            if (isSaveReadManagerConnected) {
                SaveReadManager.getInstance().saveFile(filePath, fileName, salesMark)
                button.isEnabled = true
                button2.isEnabled = false

                Toast.makeText(this, getString(R.string.result_success2), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.result_fail), Toast.LENGTH_SHORT).show()
            }
        }

        button3.setOnClickListener {
            if (isCountryCodeToolInstalled()) {
                val intent = Intent()

                intent.setComponent(
                    ComponentName(
                        countryCodePackageName,
                        "com.byd.countrycodetool.MainActivity"
                    )
                )

                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.result_conutry_code_install), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isSaveReadManagerConnected) {
            SaveReadManager.getInstance().release()
        }
    }
}