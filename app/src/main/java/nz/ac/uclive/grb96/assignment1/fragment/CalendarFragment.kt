package nz.ac.uclive.grb96.assignment1.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import nz.ac.uclive.grb96.assignment1.NotesViewModel
import nz.ac.uclive.grb96.assignment1.R
import nz.ac.uclive.grb96.assignment1.model.note.NoteSection
import nz.ac.uclive.grb96.assignment1.util.getTimeText
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarFragment : Fragment() {

    private val viewModel: NotesViewModel by activityViewModels()

    private var date = LocalDate.now()

    private lateinit var dayText: TextView
    private lateinit var calendarText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        dayText = view.findViewById(R.id.dayText)
        calendarText = view.findViewById(R.id.calendarText)

        updateText()

        val previousDayButton: Button = view.findViewById(R.id.previousDayButton)
        previousDayButton.setOnClickListener {
            date = date.minusDays(1)
            updateText()
        }

        val nextDayButton: Button = view.findViewById(R.id.nextDayButton)
        nextDayButton.setOnClickListener {
            date = date.plusDays(1)
            updateText()
        }

        return view
    }

    private fun updateText() {
        dayText.text = "${date.dayOfMonth} ${date.month} ${date.year}"
        calendarText.text = getCalendarDayText()
    }

    private fun getCalendarDayText(): SpannableStringBuilder {
        val calendarDayText = SpannableStringBuilder()
        val dueDateSection: NoteSection? = viewModel.getDueDateSectionFromDate(date)
        if (dueDateSection != null) {
            calendarDayText
                .bold { append("DUE\n") }
                .append(dueDateSection.content)
        }
        val eventSections: List<NoteSection> = viewModel.getEventSectionsFromDate(date)
        if (eventSections.isNotEmpty()) {
            calendarDayText
                .bold { append("\n\n\nEVENTS\n\n") }
            val dateTimes = arrayListOf<LocalDateTime>()
            for (section: NoteSection in eventSections) {
                dateTimes.add(section.eventTime!!.getEventsLocalDateStartTime())
            }
            val sortedDateTimes = dateTimes.sorted()
            for (dateTime: LocalDateTime in sortedDateTimes) {
                for (section: NoteSection in eventSections) {
                    if (dateTime == section.eventTime!!.getEventsLocalDateStartTime()) {
                        calendarDayText
                            .bold { append(getTimeText(section.eventTime.time)) }
                            .append("\n")
                            .append(section.content)
                            .append("\n\n")
                        break
                    }
                }
            }
        }
        if (calendarDayText.isEmpty()) {
            calendarDayText.append("Nothing due and no events.")
        }
        return calendarDayText
    }
}
