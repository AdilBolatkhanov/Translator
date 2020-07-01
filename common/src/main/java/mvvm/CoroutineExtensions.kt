package mvvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun CoroutineScope.launchSafe(
    body: suspend () -> Unit,
    handleError: (Throwable) -> Unit,
    start: (() -> Unit)? = null,
    finish: (() -> Unit)? = null
): Job =
    launch {
        try {
            start?.invoke()
            body.invoke()
        } catch (e: Throwable) {
            handleError(e)
        } finally {
            finish?.invoke()
        }
    }