package com.example.lab7task3recv

import android.content.*
import android.os.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var imagePathReceiver: BroadcastReceiver
    private var boundServiceMessenger: Messenger? = null
    private var isBound = false
    private val messenger = Messenger(ClientHandler(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent = Intent()
        intent.component = ComponentName(
            "com.example.lab7task3",
            "com.example.lab7task3.ImageService"
        )
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)

        imagePathReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                findViewById<TextView>(R.id.textLoad).text =
                    intent?.getStringExtra("message") ?: "No path =("
            }
        }
        registerReceiver(imagePathReceiver, IntentFilter("broadcastImagePath"))
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        unregisterReceiver(imagePathReceiver)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            boundServiceMessenger = null
            isBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundServiceMessenger = Messenger(service)
            isBound = true
        }
    }

    fun onBindClick(view: View) {
        val message = Message.obtain(null, 1).apply {
            replyTo = messenger
            data = Bundle().apply {
                putString(
                    "link",
                    "https://img.icons8.com/ios/452/service.png"
                )
            }
        }
        boundServiceMessenger?.send(message)
    }

    fun onStartClick(view: View) {
        intent = Intent()
        intent.component = ComponentName(
            "com.example.lab7task3",
            "com.example.lab7task3.ImageService"
        )
        intent.putExtra(
            "link",
            "https://img.icons8.com/ios/452/service.png"
        )
        startService(intent)
    }
}


private class ClientHandler(
    val context: MainActivity
) : Handler() {
    override fun handleMessage(message: Message) {
        when (message.what) {
            2 -> {
                context.findViewById<TextView>(R.id.textLoad).text =
                    message.data.getString("response")
            }
        }
    }
}