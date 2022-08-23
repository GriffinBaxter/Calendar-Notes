package nz.ac.uclive.grb96.assignment1.model.note

import nz.ac.uclive.grb96.assignment1.model.datetime.DateStartEndTime
import nz.ac.uclive.grb96.assignment1.model.datetime.YearMonthDay
import java.time.LocalDate

class NoteSection (val content: String, val header: String? = null, val dueDate: YearMonthDay? = null, val eventTime: DateStartEndTime? = null) {

    fun getDueDatesLocalDate(): LocalDate {
        return LocalDate.of(dueDate!!.year, dueDate.month + 1, dueDate.day)
    }

    override fun toString(): String {
        return header?: dueDate?.toString() ?: eventTime.toString()
    }
}
