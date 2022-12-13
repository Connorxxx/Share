package com.connor.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal open class ShareScope() : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main.immediate + SupervisorJob()

    constructor(
        lifecycleOwner: LifecycleOwner,
    ) : this() {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                if (isActive) cancel()
            }
        })
    }
}