package com.connor.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal val eventFlow = MutableSharedFlow<ShareEvent<Any>>()

@PublishedApi
internal val stickyEventFlow =
    MutableSharedFlow<ShareEvent<Any>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

internal var scope = ShareScope()

fun emitEvent(event: Any, tag: String, isSticky: Boolean = false, timeMillis: Long? = null) =
    scope.launch {
        timeMillis?.let { delay(it) }
        if (isSticky) stickyEventFlow.emit(ShareEvent(event, tag))
        else eventFlow.emit(ShareEvent(event, tag))
    }

inline fun <reified T> LifecycleOwner.lifecycleReceiveEvent(
    vararg tags: String = emptyArray(),
    dispatchers: CoroutineContext = Dispatchers.Main,
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = ShareScope(this).launch {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch(dispatchers) {
            stickyEventFlow.collect {
                if (it.event is T && tags.contains(it.tag)) {
                    block(it.event)
                }
            }
        }
        launch(dispatchers) {
            eventFlow.collect {
                if (it.event is T && tags.contains(it.tag)) {
                    block(it.event)
                }
            }
        }
    }
}

/**
 * 需要手动取消协程，推荐使用 lifecycleReceiveEvent
 */
inline fun <reified T> receiveEvent(
    vararg tags: String = emptyArray(),
    dispatchers: CoroutineContext = Dispatchers.Main,
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = ShareScope().launch {
    launch(dispatchers) {
        stickyEventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
    launch(dispatchers) {
        eventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
}
