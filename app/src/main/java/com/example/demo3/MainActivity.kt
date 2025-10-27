package com.example.demo3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var selectedItemButton: Button
    private lateinit var btnShowCustomDialog: Button

    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要通知权限才能发送通知", Toast.LENGTH_LONG).show()
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

        initViews()

        createNotificationChannel()

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    Toast.makeText(this, "需要通知权限来显示重要信息", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MyChannel"
            val descriptionText = "Channel for ListView notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("my_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initViews() {
        listView = findViewById(R.id.listView)
        selectedItemButton = findViewById(R.id.selectedItemButton)
        // 初始化对话框按钮
        btnShowCustomDialog = findViewById(R.id.btnShowCustomDialog)

        val dataList = ArrayList<HashMap<String, Any>>()
        val titles = arrayOf("Lion", "Tiger", "Monkey", "Dog", "Cat", "Elephant")
        val icons = intArrayOf(
            R.drawable.lion,      // Lion 的图片
            R.drawable.tiger,     // Tiger 的图片
            R.drawable.monkey,    // Monkey 的图片
            R.drawable.dog,       // Dog 的图片
            R.drawable.cat,       // Cat 的图片
            R.drawable.elephant   // Elephant 的图片
        )

        for (i in titles.indices) {
            val map = HashMap<String, Any>()
            map["title"] = titles[i]
            map["icon"] = icons[i]
            dataList.add(map)
        }

        // 创建Adapter
        val adapter = SimpleAdapter(
            this,
            dataList,
            R.layout.list_item,
            arrayOf("title", "icon"),
            intArrayOf(R.id.title, R.id.icon)
        )

        listView.adapter = adapter

        // 设置点击事件
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = dataList[position]["title"] as String

            // 更新按钮文字
            selectedItemButton.text = selectedItem

            Toast.makeText(this@MainActivity, "你选择了: $selectedItem", Toast.LENGTH_SHORT).show()

            sendNotification(selectedItem)
        }

        btnShowCustomDialog.setOnClickListener {
           Log.d("MainActivity", "显示对话框")
            Toast.makeText(this, "显示对话框", Toast.LENGTH_SHORT).show()
            showCustomDialog()
        }
    }

    // 显示自定义对话框
    private fun showCustomDialog() {
        Log.d("MainActivity", "showCustomDialog 方法被调用")
        Toast.makeText(this, "正在显示对话框", Toast.LENGTH_SHORT).show()

        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("登录")

            val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
            builder.setView(dialogView)

            val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
            val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            // 创建对话框
            val dialog = builder.create()

            btnConfirm.setOnClickListener {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    Toast.makeText(this, "用户名: $username, 密码: $password", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancel.setOnClickListener {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
            Log.d("MainActivity", "对话框已显示")

        } catch (e: Exception) {
            Log.e("MainActivity", "显示对话框时出错: ${e.message}")
            Toast.makeText(this, "显示对话框时出错: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendNotification(itemContent: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "需要通知权限才能发送通知", Toast.LENGTH_LONG).show()
                requestNotificationPermission()
                return
            }
        }

        try {
            val builder = NotificationCompat.Builder(this, "my_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(itemContent)
                .setContentText("这是关于 $itemContent 的详细内容")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())

        } catch (e: SecurityException) {
            Toast.makeText(this, "发送通知失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}