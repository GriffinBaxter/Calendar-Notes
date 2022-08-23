package nz.ac.uclive.grb96.assignment1.model.datestimes

import java.time.LocalTime

class StartEndTime (var startHour: Int, var startMinute: Int, var endHour: Int, var endMinute: Int) {
    fun getEventsLocalStartTime(): LocalTime {
        return LocalTime.of(startHour, startMinute)
    }

    fun getEventsLocalEndTime(): LocalTime {
        return LocalTime.of(endHour, endMinute)
    }
}
