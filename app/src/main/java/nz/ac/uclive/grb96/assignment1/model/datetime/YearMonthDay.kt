package nz.ac.uclive.grb96.assignment1.model.datetime

class YearMonthDay (var year: Int, var month: Int, var day: Int) {

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is YearMonthDay) return false
        return (year == other.year) && (month == other.month) && (day == other.day)
    }

}
