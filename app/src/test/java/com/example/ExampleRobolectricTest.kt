package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext_matchesAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickJot", appName)
    }

    @Test
    fun pinHashingAndVerification_worksReliably() {
        val pin = "1234"
        val hash = SecurityManager.hashPin(pin)
        assertTrue(SecurityManager.verifyPin("1234", hash))
        assertFalse(SecurityManager.verifyPin("0000", hash))
        assertFalse(SecurityManager.verifyPin("1235", hash))
    }
}
