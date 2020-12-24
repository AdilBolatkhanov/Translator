package com.example.common.mvp

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    protected fun Disposable.disposeOnCleared(): Disposable {
        compositeDisposable.add(this)
        return compositeDisposable
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}