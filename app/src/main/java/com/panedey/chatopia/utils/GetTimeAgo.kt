package com.panedey.chatopia.utils

import android.app.Application
import android.content.Context

class GetTimeAgo : Application() {

    companion object {
        private const val SECOND_MILLIS = 1000
        private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private const val DAY_MILLIS = 24 * HOUR_MILLIS

        fun getTimeAgo(time: Long): String? {
            var timestamp = time
            if (time < 1000000000000L) {
                // if timestamp given in seconds, convert to millis
                timestamp *= 1000
            }

            val now = System.currentTimeMillis()
            if (timestamp > now || timestamp <= 0) {
                return null
            }

            // TODO: localize
            val diff = now - timestamp
            return when {
                diff < MINUTE_MILLIS -> "just now"
                diff < 2 * MINUTE_MILLIS -> "a minute ago"
                diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} minutes ago"
                diff < 90 * MINUTE_MILLIS -> "an hour ago"
                diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} hours ago"
                diff < 48 * HOUR_MILLIS -> "yesterday"
                else -> "${diff / DAY_MILLIS} days ago"
            }
        }
    }
}
