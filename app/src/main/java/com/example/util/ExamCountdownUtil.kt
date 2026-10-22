package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExamCountdownUtil {

    // Official ICSE 2026-2027 Examination Timetable Target Dates
    const val PRE_BOARD_DATE = "2026-12-15"
    const val BOARD_EXAM_DATE = "2027-02-10"
    const val BOARD_EXAM_DISPLAY = "Feb 10, 2027"

    /**
     * Calculates days remaining between the device's current date and the target date (yyyy-MM-dd).
     * Normalizes both timestamps to midnight so partial days count accurately.
     */
    fun getDaysRemaining(targetDateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = sdf.parse(targetDateStr) ?: return 0L

            val currentCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = targetCal.timeInMillis - currentCal.timeInMillis
            if (diffMillis <= 0) return 0L

            TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun getPreBoardDaysLeft(): Long = getDaysRemaining(PRE_BOARD_DATE)

    fun getBoardExamDaysLeft(): Long = getDaysRemaining(BOARD_EXAM_DATE)
}
