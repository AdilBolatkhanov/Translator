package com.example.common;


interface MvpPresenter<V : MvpView> {

    fun attach(view: V)

    fun detach()
}