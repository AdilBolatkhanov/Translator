package com.example.common.mvp

import org.koin.core.module.Module

interface InjectionModule {
    fun create(): Module
}