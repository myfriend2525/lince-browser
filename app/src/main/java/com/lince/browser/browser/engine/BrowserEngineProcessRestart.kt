package com.lince.browser.browser.engine

import android.app.Activity
import android.content.Intent
import android.os.Process
import com.lince.browser.BrowserEngineRestartActivity

/** Restarts Candy in a fresh process so only the selected browser runtime remains resident. */
internal object BrowserEngineProcessRestart {
    fun restart(activity: Activity) {
        val started = runCatching {
            activity.startActivity(
                BrowserEngineRestartActivity.createIntent(
                    context = activity,
                    mainProcessId = Process.myPid(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.isSuccess
        if (!started) activity.recreate()
    }
}
