package com.folioreader.util

import android.content.Context
import com.folioreader.Constants
import com.folioreader.model.locators.ReadLocator

class DefaultReadLocatorManager(private val context: Context) : ReadLocatorListener {
    override fun saveReadLocator(readLocator: ReadLocator?) {
        val locator = readLocator ?: return
        SharedPreferenceUtil.putSharedPreferencesString(
            context,
            Constants.LAST_READ_LOCATOR,
            readLocator.toJson()
        )
    }

    fun getLastReadLocator(): ReadLocator? {
        return ReadLocator.fromJson(
            SharedPreferenceUtil.getSharedPreferencesString(
                context,
                Constants.LAST_READ_LOCATOR,
                null
            )
        )
    }
}