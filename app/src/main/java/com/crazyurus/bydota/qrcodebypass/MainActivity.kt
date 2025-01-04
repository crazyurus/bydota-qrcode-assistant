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

    private fun isDiLink3(): Boolean {
        val version = getDiLinkVersion();

        return version.startsWith("13.1.7")
    }


    private fun isDiLink4(): Boolean {
        val version = getDiLinkVersion();

        return version.startsWith("17") || version.startsWith("21")
    }

    private fun isDiLink5(): Boolean {
        val version = getDiLinkVersion();

        return version.startsWith("23") || version.startsWith("34")
    }

    private fun isDiLink6(): Boolean {
        val version = getDiLinkVersion();

        return version.startsWith("31")
    }

    private fun saveFile(value: String) {
        val filePath = "/collect2/cloudservice/"
        val fileName = "salesMark.dat"

        if (isDiLink4()) {
            SaveReadManager.getInstance().saveFile(filePath, fileName, value)
        } else {
            val intent = Intent("byd.intent.action.SAVE_FILE")
            intent.setPackage("com.byd.accountservice")
            intent.putExtra("file_path", filePath)
            intent.putExtra("file_name", fileName)
            intent.putExtra("str_value", value)

            sendBroadcast(intent)
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

        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val version = getDiLinkVersion()

        if (version.isEmpty()) {
            finish()
        } else {
            findViewById<TextView>(R.id.textView3).text = "当前多媒体版本 ${version}"
        }

        if (isDiLink3()) {
            button.isEnabled = false
            button2.isEnabled = false

            Toast.makeText(this, "暂不支持 DiLink 3.0 的 UI 3.0", Toast.LENGTH_SHORT).show()
        }

        if (isDiLink4()) {
            SaveReadManager.getInstance().init(this, object: SaveReadManager.IServiceConnectListener {
                override fun onBinderDied() {}
                override fun onServiceConnected() {}
                override fun onServiceDisconnected() {}
            })
        }

        if (isDiLink5() || isDiLink6()) {
            button.isEnabled = false
            button2.isEnabled = false

            Toast.makeText(this, "暂不支持 DiLink ${if (isDiLink5()) 5 else 6}.0", Toast.LENGTH_SHORT).show()
        }

        button.setOnClickListener {
            saveFile("crazyurus")

            Toast.makeText(this, "设置成功，请插入 U 盘", Toast.LENGTH_SHORT).show()
        }

        button2.setOnClickListener {
            saveFile("di_cloud_sales_m")

            Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show()
        }
    }
}