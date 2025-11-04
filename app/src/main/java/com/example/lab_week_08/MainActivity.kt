package com.example.lab_week_08

import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*
import com.example.lab_week_08.worker.*

class MainActivity : AppCompatActivity() {

    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        requestNotificationPermission()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        val firstInput = Data.Builder()
            .putString(FirstWorker.INPUT_DATA_ID, id)
            .build()

        val secondInput = Data.Builder()
            .putString(SecondWorker.INPUT_DATA_ID, id)
            .build()

        val thirdInput = Data.Builder()
            .putString(ThirdWorker.INPUT_DATA_ID, id)
            .build()

        val first = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(constraints)
            .setInputData(firstInput)
            .build()

        val second = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(constraints)
            .setInputData(secondInput)
            .build()

        val third = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setConstraints(constraints)
            .setInputData(thirdInput)
            .build()

        workManager.beginWith(first)
            .then(second)
            .then(third)
            .enqueue()

        observeWork(first, "First process is done")
        observeWork(second, "Second process is done") {
            launchNotificationService()
        }
        observeWork(third, "Third process is done") {
            launchSecondNotificationService()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun observeWork(request: OneTimeWorkRequest, message: String, onFinish: (() -> Unit)? = null) {
        workManager.getWorkInfoByIdLiveData(request.id).observe(this) { info ->
            if (info.state.isFinished) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                onFinish?.invoke()
            }
        }
    }

    private fun launchNotificationService() {
        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra(NotificationService.EXTRA_ID, "Notif_001")
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun launchSecondNotificationService() {
        val intent = Intent(this, SecondNotificationService::class.java).apply {
            putExtra(SecondNotificationService.EXTRA_ID, "Notif_002")
        }
        ContextCompat.startForegroundService(this, intent)
    }
}