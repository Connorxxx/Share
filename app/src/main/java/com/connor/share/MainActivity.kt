package com.connor.share

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.connor.core.emitEvent
import com.connor.core.receiveEvent
import com.connor.share.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        receiveEvent<String>("receive") {
            binding.tvMainRec.text = it
            Log.d("ActivityMainBinding", "onCreate: $it")
        }
        binding.btnShare.setOnClickListener {
            emitEvent("String", "sendSticky", true)
            startActivity<ReceiveActivity>(this) {}
        }
        binding.btnEvent.setOnClickListener {
            emitEvent("By Self", "receive", timeMillis = 3000)
        }
        binding.btnVmSen.setOnClickListener {
            viewModel.emitEvent("By VM", "vmReceive")
        }
        viewModel.receiveEvent<String>("vmReceive") {
            binding.tvMainRec.text = it
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("ActivityMainBinding", "onStop: ")
    }

    inline fun <reified T> startActivity(context: Context, block: Intent.() -> Unit) {
        val intent = Intent(context, T::class.java)
        intent.block()
        context.startActivity(intent)
    }
}