package com.example.remotecontrolserver

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.remotecontrolserver.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val SCREEN_REQUEST_CODE = 1001
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startServerButton.setOnClickListener {
            if (!isRunning) {
                requestScreenCapture()
            }
        }
        binding.stopServerButton.setOnClickListener {
            stopServices()
        }
    }

    private fun requestScreenCapture() {
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mgr.createScreenCaptureIntent()
        startActivityForResult(intent, SCREEN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // create pairing code and show QR
                CoroutineScope(Dispatchers.IO).launch {
                    val (code, keysetBytes) = PairingManager.createPairing(this@MainActivity)
                    val ip = NetworkUtils.getLocalIpAddress(this@MainActivity) ?: "0.0.0.0"
                    val uri = "remote://$ip:5000?code=$code"
                    val bmp = QRGenerator.generate(uri)
                    runOnUiThread {
                        binding.qrCodeImageView.setImageBitmap(bmp)
                        binding.statusTextView.text = getString(R.string.status_running, ip, 5000)
                    }

                    // start services
                    val svc = Intent(this@MainActivity, ScreenCaptureService::class.java)
                    svc.putExtra("resultCode", resultCode)
                    svc.putExtra("data", data)
                    startForegroundService(svc)
                    isRunning = true
                }
            } else {
                Timber.i("Screen capture denied")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun stopServices() {
        stopService(Intent(this, ScreenCaptureService::class.java))
        isRunning = false
        binding.statusTextView.text = getString(R.string.status_not_running)
        binding.qrCodeImageView.setImageDrawable(null)
    }
}