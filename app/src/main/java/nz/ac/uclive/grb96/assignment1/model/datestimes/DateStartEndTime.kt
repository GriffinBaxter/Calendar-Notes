package nz.ac.uclive.grb96.assignment1.model.datestimes

import java.time.LocalDateTime

class DateStartEndTime (val date: YearMonthDay, var time: StartEndTime) {
    fun getEventsLocalDateStartTime(): LocalDateTime {
        return LocalDateTime.of(date.year, date.month + 1, date.day, time.startHour, time.startMinute)
    }
}
