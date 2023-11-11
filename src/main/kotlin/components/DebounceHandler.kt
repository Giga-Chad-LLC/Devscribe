package components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebounceHandler<T>(
    private val waitMs: Long,
    private val scope: CoroutineScope,
    private val callback: (T) -> Unit
) {
    private var debounceJob: Job? = null

    fun run(param: T) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(waitMs)
            callback(param)
        }
    }
}