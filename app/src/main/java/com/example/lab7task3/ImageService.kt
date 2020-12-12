package com.example.lab7task3

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class ImageService : IntentService("ImageService") {
    private lateinit var messenger: Messenger

    override fun onBind(intent: Intent): IBinder? {
        messenger = Messenger(ServiceHandler(this))
        return messenger.binder
    }

    override fun onHandleIntent(intent: Intent?) =
        sendBroadcast(
            Intent("broadcastImagePath").putExtra(
                "message",
                downloadPath(intent?.getStringExtra("link"))
            )
        )

    fun downloadPath(url: String?): String {
        val res: String
        res = try {
            val bitmap = BitmapFactory.decodeStream(java.net.URL(url).openStream())
            val fos: FileOutputStream =
                openFileOutput("Bound Service Image.png", MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            getFileStreamPath("Bound Service Image.png").absolutePath
        } catch (e: Exception) {
            ""
        }
        return res
    }
}

private class ServiceHandler(private val serviceContext: ImageService) : Handler() {
    override fun handleMessage(message: Message) {
        when (message.what) {
            1 -> {
                DownloadImageTask(message.replyTo, serviceContext).execute(
                    message.data.getString(
                        "link",
                        "https://static.thenounproject.com/png/409652-200.png"
                    )
                )
            }
        }
    }
}


private class DownloadImageTask(
    private val activityMessenger: Messenger? = null, context: ImageService,
    private val serviceReference: WeakReference<ImageService> = WeakReference(
        context
    )
) :
    AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg urls: String): String? {
        val service = serviceReference.get() ?: return ""
        return service.downloadPath(urls[0])
    }

    override fun onPostExecute(path: String) {
        super.onPostExecute(path)
        val message = Message.obtain(null, 2).apply {
            data = Bundle().apply { putString("response", path) }
        }
        activityMessenger?.send(message)
    }
}