package com.example.demo3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ActionMode
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
import java.util.LinkedList

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var selectedItemButton: Button
    private lateinit var btnShowCustomDialog: Button
    private lateinit var tvTestContent: TextView

    // 新增变量：用于跟踪当前的 ActionMode 和选中的列表项
    private var currentActionMode: ActionMode? = null
    private val selectedItems = LinkedList<Int>() // 存储选中项的位置

    // 新增：实现 ActionMode.Callback 接口
    private val actionModeCallback = object : ActionMode.Callback {
        // 当 ActionMode 创建时调用（这里填充我们的菜单）
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.let {
                menuInflater.inflate(R.menu.context_menu, menu)
                it.title = "${selectedItems.size} selected" // 显示选中数量
            }
            return true
        }

        // 在创建后准备显示前调用
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // 返回 false 表示不需要重新创建
        }

        // 当用户点击上下文操作栏上的菜单项时调用
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.menu_delete -> {
                    // 处理删除逻辑
                    deleteSelectedItems()
                    mode?.finish() // 操作完成后结束 ActionMode
                    true
                }
                R.id.menu_select_all -> {
                    // 处理全选逻辑
                    selectAllItems()
                    mode?.invalidate() // 更新 ActionMode 的UI
                    true
                }
                else -> false
            }
        }

        // 当用户退出 ActionMode 时调用
        override fun onDestroyActionMode(mode: ActionMode?) {
            // 清理选中状态
            selectedItems.clear()
            updateListViewSelection()
            currentActionMode = null
        }
    }

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

        // 移除 Toolbar 相关代码

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        createNotificationChannel()

        requestNotificationPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // 处理菜单项点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 字体大小 - 小
            R.id.menu_font_small -> {
                tvTestContent.textSize = 10f
                Toast.makeText(this, "已设置为小号字体", Toast.LENGTH_SHORT).show()
                return true
            }
            // 字体大小 - 中
            R.id.menu_font_medium -> {
                tvTestContent.textSize = 16f
                Toast.makeText(this, "已设置为中号字体", Toast.LENGTH_SHORT).show()
                return true
            }
            // 字体大小 - 大
            R.id.menu_font_large -> {
                tvTestContent.textSize = 20f
                Toast.makeText(this, "已设置为大号字体", Toast.LENGTH_SHORT).show()
                return true
            }
            // 普通菜单项
            R.id.menu_normal -> {
                Toast.makeText(this, "你点击了普通菜单项", Toast.LENGTH_SHORT).show()
                return true
            }
            // 字体颜色 - 红色
            R.id.menu_color_red -> {
                tvTestContent.setTextColor(Color.RED)
                Toast.makeText(this, "已设置为红色", Toast.LENGTH_SHORT).show()
                return true
            }
            // 字体颜色 - 黑色
            R.id.menu_color_black -> {
                tvTestContent.setTextColor(Color.BLACK)
                Toast.makeText(this, "已设置为黑色", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
        btnShowCustomDialog = findViewById(R.id.btnShowCustomDialog)
        tvTestContent = findViewById(R.id.tvTestContent)

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
            // 如果正在 ActionMode 中，则处理选中/取消选中
            if (currentActionMode != null) {
                toggleSelection(position)
            } else {
                val selectedItem = dataList[position]["title"] as String

                // 更新按钮文字
                selectedItemButton.text = selectedItem

                Toast.makeText(this@MainActivity, "你选择了: $selectedItem", Toast.LENGTH_SHORT).show()

                sendNotification(selectedItem)
            }
        }

        // 设置列表项的长按监听器以启动上下文操作模式
        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            if (currentActionMode == null) {
                // 启动新的 ActionMode
                currentActionMode = startActionMode(actionModeCallback)
            }
            toggleSelection(position) // 切换该项的选中状态
            true // 返回 true 表示消费了长按事件
        }

        btnShowCustomDialog.setOnClickListener {
            Log.d("MainActivity", "显示对话框")
            Toast.makeText(this, "显示对话框", Toast.LENGTH_SHORT).show()
            showCustomDialog()
        }
    }

    // 新增：切换列表项选中状态的辅助方法
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        updateListViewSelection()

        // 更新 ActionMode 标题
        currentActionMode?.title = "${selectedItems.size} selected"

        // 如果没有选中的项，则结束 ActionMode
        if (selectedItems.isEmpty()) {
            currentActionMode?.finish()
        }
    }

    // 修复：更新列表视图的选中状态（视觉反馈）
    private fun updateListViewSelection() {
        // 获取当前可见的列表项范围
        val firstVisiblePosition = listView.firstVisiblePosition
        val lastVisiblePosition = listView.lastVisiblePosition

        for (i in firstVisiblePosition..lastVisiblePosition) {
            val itemView = listView.getChildAt(i - firstVisiblePosition)
            itemView?.setBackgroundColor(
                if (selectedItems.contains(i)) {
                    ContextCompat.getColor(this, android.R.color.holo_blue_light)
                } else {
                    Color.TRANSPARENT
                }
            )
        }
    }

    // 新增：删除选中的项
    private fun deleteSelectedItems() {
        if (selectedItems.isNotEmpty()) {
            Toast.makeText(this, "删除 ${selectedItems.size} 个选项", Toast.LENGTH_SHORT).show()
            // 注意：在实际应用中，你需要在这里更新你的数据列表并通知适配器
        }
    }

    // 新增：全选项
    private fun selectAllItems() {
        selectedItems.clear()
        for (i in 0 until listView.count) {
            selectedItems.add(i)
        }
        updateListViewSelection()
        currentActionMode?.title = "${selectedItems.size} selected"
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