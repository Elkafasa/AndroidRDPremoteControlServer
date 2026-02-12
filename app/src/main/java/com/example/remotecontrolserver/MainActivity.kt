package com.example.remotecontrolserver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.remotecontrolserver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.statusTextView.text = "Server Idle (0.1.0)"
    }
}
