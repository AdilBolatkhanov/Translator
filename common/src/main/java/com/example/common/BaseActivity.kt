package com.example.common

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

abstract class BaseActivity : AppCompatActivity() {

    fun replaceFragment(
        fragment: Fragment,
        @IdRes layoutId: Int = R.id.container,
        addToBackStack: Boolean = true
    ) {
        supportFragmentManager
            .beginTransaction()
            .replace(layoutId, fragment)
            .apply { if (addToBackStack) addToBackStack(fragment::class.java.simpleName) }
            .commit()
    }

    fun addFragment(
        fragment: Fragment,
        @IdRes layoutId: Int = R.id.container,
        addToBackStack: Boolean = true,
        tag: String = fragment::class.java.simpleName
    ) {
        supportFragmentManager
            .beginTransaction()
            .add(layoutId, fragment)
            .apply { if (addToBackStack) addToBackStack(tag) }
            .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun clearBackstack() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}