package com.connor.share

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.connor.core.lifecycleReceiveEvent
import com.connor.core.emitEvent
import com.connor.share.databinding.ActivityReceiveBinding

class ReceiveActivity : AppCompatActivity() {
    private val binding by lazy { ActivityReceiveBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        lifecycleReceiveEvent<String>("sendSticky") {
            binding.tvReceive.text = it
        }
        binding.btnSendMain.setOnClickListener {
            emitEvent("From Receive", "receive", true)
            onBackPressed()
        }
    }
}