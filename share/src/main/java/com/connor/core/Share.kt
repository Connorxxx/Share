package com.connor.core

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal val eventFlow = MutableSharedFlow<ShareEvent<Any>>()

@PublishedApi
internal val stickyEventFlow =
    MutableSharedFlow<ShareEvent<Any>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

internal var scope = ShareScope()

/**
 * 发送事件
 */
fun emitEvent(event: Any, tag: String, isSticky: Boolean = false, timeMillis: Long? = null) =
    scope.launch {
        timeMillis?.let { delay(it) }
        if (isSticky) stickyEventFlow.emit(ShareEvent(event, tag))
        else eventFlow.emit(ShareEvent(event, tag))
    }

/**
 * 非 lifecycle 接收事件，需要手动取消
 */
inline fun <reified T> receiveEvent(
    vararg tags: String = emptyArray(),
    dispatchers: CoroutineContext = Dispatchers.Main.immediate,
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = ShareScope().launch(dispatchers) {
    launch {
        stickyEventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
    launch {
        eventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
}

/**
 * lifecycle 发送事件
 */
fun LifecycleOwner.emitEvent(
    event: Any,
    tag: String,
    isSticky: Boolean = false,
    timeMillis: Long? = null
) = lifecycleScope.launch {
    timeMillis?.let { delay(it) }
    if (isSticky) stickyEventFlow.emit(ShareEvent(event, tag))
    else eventFlow.emit(ShareEvent(event, tag))
}

/**
 * lifecycle 接收事件
 */
inline fun <reified T> LifecycleOwner.receiveEvent(
    vararg tags: String = emptyArray(),
    dispatchers: CoroutineContext = Dispatchers.Main.immediate,
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = lifecycleScope.launch(dispatchers) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            stickyEventFlow.collect {
                if (it.event is T && tags.contains(it.tag)) {
                    block(it.event)
                }
            }
        }
        launch {
            eventFlow.collect {
                if (it.event is T && tags.contains(it.tag)) {
                    block(it.event)
                }
            }
        }
    }
}

/**
 * viewModel 发送事件
 */
fun ViewModel.emitEvent(
    event: Any,
    tag: String,
    isSticky: Boolean = false,
    timeMillis: Long? = null
) = viewModelScope.launch {
    timeMillis?.let { delay(it) }
    if (isSticky) stickyEventFlow.emit(ShareEvent(event, tag))
    else eventFlow.emit(ShareEvent(event, tag))
}


/**
 * viewModel 接收事件
 */
inline fun <reified T> ViewModel.receiveEvent(
    vararg tags: String = emptyArray(),
    dispatchers: CoroutineContext = Dispatchers.Main.immediate,
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = viewModelScope.launch(dispatchers) {
    launch {
        stickyEventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
    launch {
        eventFlow.collect {
            if (it.event is T && tags.contains(it.tag)) {
                block(it.event)
            }
        }
    }
}
