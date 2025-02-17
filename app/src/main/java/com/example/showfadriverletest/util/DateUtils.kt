package com.example.showfadriverletest.util

import android.annotation.SuppressLint
import android.os.Build
import android.text.format.DateFormat
import java.math.BigDecimal
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object DateUtils {
    const val DEFAULT_FORMAT_DATE_WITHOUT_TIME = "dd-MM-YYYY"
    const val DEFAULT_FORMAT_DATE = "dd-MM-yyyy HH:mm"
    const val USA_FORMAT_DATE = "MM-dd-yyyy hh:mm a"
    const val FILE_SAVE_DATE_FORMAT = "yyyyMMdd_HHmmss"
    val BASETIME = BigDecimal("60.00")
    val DATE_PATTERN =
        Pattern.compile("^(?:(?!0000)[0-9]{4}([-/.]?)(?:(?:0?[1-9]|1[0-2])([-/.]?)(?:0?[1-9]|1[0-9]|2[0-8])|(?:0?[13-9]|1[0-2])([-/.]?)(?:29|30)|(?:0?[13578]|1[02])([-/.]?)31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)([-/.]?)0?2([-/.]?)29)$")

    fun validateDateFormat(dateText: String?): Boolean {
        return DATE_PATTERN.matcher(dateText).matches()
    }

    fun now(): Timestamp {
        return Timestamp(System.currentTimeMillis())
    }

    fun setDate(startDate: Date, amount: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DATE, amount)
        return dateFormat.format(calendar.time)
    }

    fun nextDate(startDate: Date, amount: Int): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DATE, +amount)
        return dateFormat.format(calendar.time)
    }

    fun timeStampToUsaDate(dateInMilliseconds: String, dateFormat: String?): String {
        return DateFormat.format(dateFormat, dateInMilliseconds.toLong() * 1000).toString()
    }

    fun resetTime(date: Date?): Date {
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        return c.time
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: Date?, formatStr: String?): String {
        return SimpleDateFormat(formatStr ?: DEFAULT_FORMAT_DATE).format(date!!)
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(ParseException::class)
    fun formatDate(dateStr: String?, formatStr: String?): Date {
        return SimpleDateFormat(formatStr ?: DEFAULT_FORMAT_DATE).parse(dateStr!!)!!
    }

    fun formatTime(dateStr: String?): Timestamp {
        return Timestamp.valueOf(dateStr)
    }

    fun getYear(date: Date?): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.YEAR]
    }

    fun getMonth(date: Date?): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.MONTH] + 1
    }

    @Throws(ParseException::class)
    fun getYear(dateStr: String?, format: String?) = getYear(formatDate(dateStr, format))

    @Throws(ParseException::class)
    fun getMonth(dateStr: String?, format: String?) = getMonth(formatDate(dateStr, format))

    @Throws(ParseException::class)
    fun getDay(dateStr: String?, format: String?) = getDay(formatDate(dateStr, format))

    fun getDay(date: Date?): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.DAY_OF_MONTH]
    }

    fun getWeekDay(date: Date?): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.DAY_OF_WEEK]
    }

    @Throws(ParseException::class)
    fun getWeekDay(dateStr: String?, format: String?) = getWeekDay(formatDate(dateStr, format))

    fun getDayCountInMonth(month: Int, isLeapYear: Boolean): Int {
        var dayCount = 0
        if (month >= Calendar.JANUARY && month <= Calendar.DECEMBER) {
            if (month == Calendar.JANUARY || month == Calendar.MARCH || month == Calendar.MAY || month == Calendar.JULY || month == Calendar.AUGUST || month == Calendar.OCTOBER || month == Calendar.DECEMBER
            ) {
                dayCount = 31
            } else if (month == Calendar.APRIL || month == Calendar.JUNE || month == Calendar.SEPTEMBER || month == Calendar.NOVEMBER
            ) {
                dayCount = 30
            } else if (month == Calendar.FEBRUARY) {
                dayCount = if (isLeapYear) {
                    29
                } else {
                    28
                }
            }
        }
        return dayCount
    }

    fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 || year % 100 != 0 && year % 400 == 0
    }

    fun compareDay(d1: Date?, d2: Date?): Boolean {
        return resetTime(d1).after(resetTime(d2))
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @return
     */
    fun daySub(startDate: Date, endDate: Date): Long {
        return if (startDate.time - endDate.time > 0) (startDate
            .time - endDate.time) / 86400000 else (endDate.time - startDate.time) / 86400000
    }

    fun addOneDay(date: Date?): Date {
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.DAY_OF_MONTH] = c[Calendar.DAY_OF_MONTH] + 1
        return resetTime(c.time)
    }

    fun lessenOneDay(date: Date?): Date {
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.DAY_OF_MONTH] = c[Calendar.DAY_OF_MONTH] - 1
        return resetTime(c.time)
    }

    @Throws(Exception::class)
    fun lessenOneDay(dateStr: String?): String {
        val date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.DAY_OF_MONTH] = c[Calendar.DAY_OF_MONTH] - 1
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return df.format(resetTime(c.time))
    }

    fun getFormatDateWithoutMillSecond(time: String?): String? {
        return if (time == null || time.trim { it <= ' ' }.isEmpty()) {
            null
        } else time.substring(0, 19)
    }

    /*
     * Get start time by year and month
     */
    fun getDateFromYearAndMonth(year: Int, month: Int): Date? {
        var year = year
        var month = month
        if (month > 12) {
            year++
            month -= 12
        }
        var resultDate = Date(year - 1900, month - 1, 1)
        resultDate = resetTime(resultDate)
        return resultDate
    }

    fun getDateFromSeason(year: Int, season: Int): Date? {
        var year = year
        var season = season
        if (season < 1) {
            throw RuntimeException("must be greater than 1")
        }
        if (season > 4) {
            year += season / 4
            season %= 4
        }
        val month = (season - 1) * 3 + 1
        return getDateFromYearAndMonth(year, month)
    }

    fun getLastMomentOfDay(original: Date?): Date {
        val calendar = Calendar.getInstance()
        calendar.time = original
        calendar[Calendar.HOUR_OF_DAY] = calendar.getActualMaximum(Calendar.HOUR_OF_DAY)
        calendar[Calendar.MINUTE] = calendar.getActualMaximum(Calendar.MINUTE)
        calendar[Calendar.SECOND] = calendar.getActualMaximum(Calendar.SECOND)
        calendar[Calendar.MILLISECOND] =
            0 // If this exceeds 0, it will be carried and will become the next day
        return calendar.time
    }

    /**
     * Format (from, start time, etc.) time to facilitate hql time query comparison
     */
    fun formateStartTime(startTime: String): String {
        return "$startTime 00:00:00"
    }

    /**
     * Format (to, end time, etc.) time to facilitate hql time query comparison
     */
    fun formateEndTime(endTime: String): String {
        return "$endTime 23:59:59"
    }

    /**
     * Get the date string of the last day of a year and month (such as 2013-08-31)
     */
    fun getLastDayText(year: Int?, month: Int?): String {
        return formatDate(getLastDay(year, month), "$DEFAULT_FORMAT_DATE_WITHOUT_TIME 23:59:59")
    }

    fun getLastDay(year: Int?, month: Int?): Date {
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year!!
        cal[Calendar.MONTH] = month!!
        cal[Calendar.DAY_OF_MONTH] = 1
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.time
    }

    //The last day of a month
    fun getLastDay(dateStr: String?): String {
        val cal = Calendar.getInstance()
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        try {
            val date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            cal.time = date
            cal.add(Calendar.MONTH, 1)
            cal[Calendar.DAY_OF_MONTH] = 1
            cal.add(Calendar.DAY_OF_MONTH, -1)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return df.format(cal.time)
    }

    /**
     *Get the date string of the first day of a year and month (such as 2013-08-01)
     */
    fun getFirstDayText(year: Int, month: Int): String {
        return if (month < 10) {
            "$year-0$month-01 00:00:00"
        } else "$year-$month-01 00:00:00"
    }

    fun getFirstDay(year: Int, month: Int): Date? {
        return try {
            formatDate("$year-$month-1", DEFAULT_FORMAT_DATE_WITHOUT_TIME)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //The first day of a month
    fun getFirstDay(dateStr: String?): String {
        val cal = Calendar.getInstance()
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        try {
            val date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            cal.time = date
            cal[Calendar.DAY_OF_MONTH] = 1
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return df.format(cal.time)
    }

    /**
     * Get a date before a month
     */
    fun getBeforeMonthDate(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, -1)
        return cal.time
    }

    fun addOneSecond(time: Date): Date {
        return Timestamp(time.time + 1000)
    }

    /**
     * Take the first day of the week of the current day, Sunday
     * @param args
     */
    fun getFirstWeekDay(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY // Sunday of the first day of the week
        return cal.time
    }

    fun getFirstWeekDay(dateStr: String?): Date? {
        var date: Date? = null
        try {
            date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            date = getFirstWeekDay(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun getFirstWeekDayStr(dateStr: String?): String {
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return df.format(getFirstWeekDay(dateStr))
    }

    /**
     * Take the last day of the week of the current day, Saturday
     * @param args
     */
    fun getLastWeekDay(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_WEEK] = Calendar.SATURDAY //Saturday of the last day of the week
        return cal.time
    }

    fun getLastWeekDay(dateStr: String?): Date? {
        var date: Date? = null
        try {
            date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            date = getLastWeekDay(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun getLastWeekDayStr(dateStr: String?): String {
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return df.format(getLastWeekDay(dateStr))
    }

    /**
     * Take the first day of the quarter
     * @param args
     */
    fun getFirstQuarter(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        val month = getQuarterInMonth(cal[Calendar.MONTH], true)
        cal[Calendar.MONTH] = month - 1
        cal[Calendar.DAY_OF_MONTH] = 1
        return cal.time
    }

    fun getFirstQuarter(dateStr: String?): Date? {
        var date: Date? = null
        try {
            date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            date = getFirstQuarter(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun getFirstQuarterStr(dateStr: String?): String {
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return df.format(getFirstQuarter(dateStr))
    }

    /**
     *Take the last day of the quarter on the date
     * @param args
     */
    fun getLastQuarter(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        val month = getQuarterInMonth(cal[Calendar.MONTH], false)
        cal[Calendar.MONTH] = month
        cal[Calendar.DAY_OF_MONTH] = 1
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.time
    }

    fun getLastQuarter(dateStr: String?): Date? {
        var date: Date? = null
        try {
            date = formatDate(dateStr, DEFAULT_FORMAT_DATE_WITHOUT_TIME)
            date = getLastQuarter(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun getLastQuarterStr(dateStr: String?): String {
        val df = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(DEFAULT_FORMAT_DATE_WITHOUT_TIME, Locale.getDefault())
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        return df.format(getLastQuarter(dateStr)!!)
    }

    //Returns the first few months, not several months
    // Quarters throughout the year, first quarter: February-April, second quarter: May-July, third quarter: August-October, fourth quarter: November-January
    private fun getQuarterInMonth(month: Int, isQuarterStart: Boolean): Int {
        var months = intArrayOf(1, 4, 7, 10)
        if (!isQuarterStart) {
            months = intArrayOf(3, 6, 9, 12)
        }
        return if (month in 0..2) months[0] else if (month in 3..5) months[1] else if (month in 6..8) months[2] else months[3]
    }

    /**
     * String to timestamp
     */
    @Throws(Exception::class)
    fun formatTimestamp(time: String?, formatStr: String?): Timestamp {
        val date = formatDate(time, formatStr)
        return Timestamp(date.time)
    }

    /**
     * date to timestamp
     */
    @Throws(Exception::class)
    fun formatTimestamp(date: Date): Timestamp {
        return Timestamp(date.time)
    }

}