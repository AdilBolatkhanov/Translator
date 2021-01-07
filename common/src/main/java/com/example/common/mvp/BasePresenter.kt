package com.example.common.mvp

import androidx.annotation.CallSuper

abstract class BasePresenter<V : MvpView> : BaseViewModel(), MvpPresenter<V> {

    protected var view: V? = null
        private set

    @CallSuper
    override fun attach(view: V) {
        this.view = view
    }

    @CallSuper
    override fun detach() {
        view = null
    }
}