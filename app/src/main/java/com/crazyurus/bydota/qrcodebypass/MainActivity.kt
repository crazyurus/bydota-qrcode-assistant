package com.crazyurus.bydota.qrcodebypass

import android.os.Bundle
import android.os.Debug
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
    private var isSaveReadManagerConnected = false

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

    private fun checkStatus() {
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        if (SaveReadManager.getInstance().readFile(filePath + fileName) == salesMark) {
            button.isEnabled = true
            button2.isEnabled = false
        } else {
            button.isEnabled = false
            button2.isEnabled = true

            Toast.makeText(this, "已设置跳过二维码，请插入 U 盘", Toast.LENGTH_SHORT).show()
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
        val textView3 = findViewById<TextView>(R.id.textView3)
        val textVersion = findViewById<TextView>(R.id.textVersion)
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        if (version.isEmpty()) {
            finish()
        } else {
            textView3.text = "❌ 不支持当前多媒体版本 ${version}"
        }

        textVersion.text = "版本：" + packageManager.getPackageInfo(packageName, 0).versionName
        SaveReadManager.getInstance().init(this, object : SaveReadManager.IServiceConnectListener {
            override fun onBinderDied() {}

            override fun onServiceConnected() {
                checkStatus()

                isSaveReadManagerConnected = true
                textView3.text = "✅ 支持当前多媒体版本 ${version}"
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

                Toast.makeText(this, "设置成功，请插入 U 盘", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show()
            }
        }

        button2.setOnClickListener {
            if (isSaveReadManagerConnected) {
                SaveReadManager.getInstance().saveFile(filePath, fileName, salesMark)
                button.isEnabled = true
                button2.isEnabled = false

                Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show()
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