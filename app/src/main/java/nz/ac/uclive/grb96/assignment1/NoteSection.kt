package nz.ac.uclive.grb96.assignment1

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NoteSection (val content: String, val header: String? = null, val dueDate: YearMonthDay? = null, val eventTime: DateStartEndHourMinute? = null) {
    fun getDueDatesLocalDate(): LocalDate {
        return LocalDate.of(dueDate!!.year, dueDate.month + 1, dueDate.day)
    }
}

class YearMonthDay (var year: Int, var month: Int, var day: Int) {}

class DateStartEndHourMinute (val date: YearMonthDay, var startHour: Int, var startMinute: Int, var endHour: Int, var endMinute: Int) {
    fun getEventsLocalDateTimeStart(): LocalDateTime {
        return LocalDateTime.of(date.year, date.month + 1, date.day, startHour, startMinute)
    }

    fun getEventsLocalTimeStart(): LocalTime {
        return LocalTime.of(startHour, startMinute)
    }

    fun getEventsLocalTimeEnd(): LocalTime {
        return LocalTime.of(endHour, endMinute)
    }
}
