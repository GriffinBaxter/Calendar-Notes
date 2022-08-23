package nz.ac.uclive.grb96.assignment1.model.datetime

import java.time.LocalDateTime

class DateStartEndTime (val date: YearMonthDay, var time: StartEndTime) {

    fun getEventsLocalDateStartTime(): LocalDateTime {
        return LocalDateTime.of(date.year, date.month + 1, date.day, time.startHour, time.startMinute)
    }

    override fun toString(): String {
        return "$date ($time)"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is DateStartEndTime) return false
        return (date.year == other.date.year) && (date.month == other.date.month) && (date.day == other.date.day) && (time.startHour == other.time.startHour) && (time.startMinute == other.time.startMinute) && (time.endHour == other.time.endHour) && (time.endMinute == other.time.endMinute)
    }

}
