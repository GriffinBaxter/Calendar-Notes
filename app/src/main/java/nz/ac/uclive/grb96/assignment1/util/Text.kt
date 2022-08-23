package nz.ac.uclive.grb96.assignment1.util

import nz.ac.uclive.grb96.assignment1.model.datetime.StartEndTime

fun getTimeText(startEndTime: StartEndTime): String {
    return startEndTime.getEventsLocalStartTime().toString() + " - " + startEndTime.getEventsLocalEndTime().toString()
}
