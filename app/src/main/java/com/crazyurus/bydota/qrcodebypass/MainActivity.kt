package com.crazyurus.bydota.qrcodebypass

import android.content.Intent
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
    private var supportSaveReadManager = false

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

    private fun isDiLinkSupport(): Boolean {
        val version = getDiLinkVersion()
        val whiteList = arrayOf(
            "13.1.22",
            "15.1.22",
            "16.",
            "17.",
            "18.",
            "21.",
            "23."
        )

        return whiteList.find { version.startsWith(it) } != null
    }

    private fun isNotDiLinkSupport(): Boolean {
        val version = getDiLinkVersion()
        val whiteList = arrayOf(
            "2.",
            "4.",
            "5.",
            "7.",
            "13.1.7",
            "15.1.7",
            "26.",
            "36.",
            "42."
        )

        return whiteList.find { version.startsWith(it) } != null
    }

    private fun checkStatus() {
        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        if (SaveReadManager.getInstance().readFile(filePath + fileName) == "di_cloud_sales_m") {
            button.isEnabled = true
            button2.isEnabled = false
        } else {
            button.isEnabled = false
            button2.isEnabled = true
        }
    }

    private fun saveFileAction(value: String) {
        val intent = Intent("byd.intent.action.SAVE_FILE")

        intent.setPackage("com.byd.accountservice")
        intent.putExtra("file_path", filePath)
        intent.putExtra("file_name", fileName)
        intent.putExtra("str_value", value)

        sendBroadcast(intent)
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

        if (version.isEmpty()) {
            finish()
        } else {
            textView3.text = "当前多媒体版本 ${version}"
        }

        textVersion.text = "版本：" + packageManager.getPackageInfo(packageName, 0).versionName

        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)

        if (isDiLinkSupport()) {
            textView3.text = "✅ 支持" + textView3.text

            SaveReadManager.getInstance().init(this, object : SaveReadManager.IServiceConnectListener {
                override fun onBinderDied() {}

                override fun onServiceConnected() {
                    checkStatus()
                    supportSaveReadManager = true
                }

                override fun onServiceDisconnected() {
                    finish()
                }
            })
        } else if (isNotDiLinkSupport()) {
            button.isEnabled = false
            button2.isEnabled = false

            textView3.text = "❌ 不支持" + textView3.text

            Toast.makeText(this, "暂不支持你的 DiLink 版本", Toast.LENGTH_SHORT).show()
        } else {
            textView3.text = "可能支持" + textView3.text
        }

        button.setOnClickListener {
            val value = "crazyurus"

            if (supportSaveReadManager) {
                SaveReadManager.getInstance().saveFile(filePath, fileName, value)
                checkStatus()
            } else {
                saveFileAction(value)
            }

            Toast.makeText(this, "设置成功，请插入 U 盘", Toast.LENGTH_SHORT).show()
        }

        button2.setOnClickListener {
            val value = "di_cloud_sales_m"

            if (supportSaveReadManager) {
                SaveReadManager.getInstance().saveFile(filePath, fileName, value)
                checkStatus()
            } else {
                saveFileAction(value)
            }

            Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show()
        }
    }
}