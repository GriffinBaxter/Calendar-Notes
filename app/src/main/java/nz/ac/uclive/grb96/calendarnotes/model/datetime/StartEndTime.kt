package nz.ac.uclive.grb96.calendarnotes.model.datetime

import java.time.LocalTime

class StartEndTime (var startHour: Int, var startMinute: Int, var endHour: Int, var endMinute: Int) {
    fun getEventsLocalStartTime(): LocalTime {
        return LocalTime.of(startHour, startMinute)
    }

    fun getEventsLocalEndTime(): LocalTime {
        return LocalTime.of(endHour, endMinute)
    }

    override fun toString(): String {
        return "${getEventsLocalStartTime()} - ${getEventsLocalEndTime()}"
    }
}
