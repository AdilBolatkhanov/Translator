package mvvm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

inline fun <T> LiveData<T>.observeNotNull(owner: LifecycleOwner, crossinline observer: (T) -> Unit) {
    this.observe(owner, Observer { it?.run(observer) })
}

inline fun <T : Any, L : LiveData<T>> LifecycleOwner.observeNotNull(
        liveData: L, crossinline body: (T) -> Unit) =
        liveData.observe(this, Observer{ it?.run(body) })