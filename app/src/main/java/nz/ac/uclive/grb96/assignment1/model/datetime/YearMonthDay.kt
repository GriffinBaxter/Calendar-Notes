package nz.ac.uclive.grb96.assignment1.model.datetime

import java.time.LocalDate

class YearMonthDay (var year: Int, var month: Int, var day: Int) {

    fun getYearMonthDayLocalDate(): LocalDate {
        return LocalDate.of(year, month + 1, day)
    }

    override fun toString(): String {
        val localDate = getYearMonthDayLocalDate()
        return "$day ${localDate.month.name} $year"
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is YearMonthDay) return false
        return (year == other.year) && (month == other.month) && (day == other.day)
    }

}
