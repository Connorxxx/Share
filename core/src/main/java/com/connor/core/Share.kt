package com.connor.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@PublishedApi
internal val eventFlow = MutableSharedFlow<ShareEvent<Any>>(extraBufferCapacity = Int.MAX_VALUE)

@PublishedApi
internal val stickyEventFlow =
    MutableSharedFlow<ShareEvent<Any>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

internal var scope = ShareScope()

fun sendEvent(event: Any, tag: String? = null, isSticky: Boolean = false) = scope.launch {
    if (isSticky) stickyEventFlow.emit(ShareEvent(event, tag))
    else eventFlow.emit(ShareEvent(event, tag))
}

inline fun <reified T> LifecycleOwner.receiveEvent(
    vararg tags: String? = emptyArray(),
    crossinline block: suspend CoroutineScope.(event: T) -> Unit
) = ShareScope(this).launch {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            stickyEventFlow.collect {
                if (it.event is T && (tags.isEmpty() || tags.contains(it.tag))) {
                    block(it.event)
                }
            }
        }
        launch {
            eventFlow.collect {
                if (it.event is T && (tags.isEmpty() || tags.contains(it.tag))) {
                    block(it.event)
                }
            }
        }
    }
}
