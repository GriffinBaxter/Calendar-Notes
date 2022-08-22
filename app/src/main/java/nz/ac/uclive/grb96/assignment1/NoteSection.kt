package nz.ac.uclive.grb96.assignment1

import java.time.LocalDate

class NoteSection (val content: String, val header: String? = null, val dueDate: DayMonthYear? = null) {
    fun getLocalDate(): LocalDate {
        return LocalDate.of(dueDate!!.year, dueDate.month + 1, dueDate.day)
    }
}
