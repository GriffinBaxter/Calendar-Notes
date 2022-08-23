package nz.ac.uclive.grb96.assignment1.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.ac.uclive.grb96.assignment1.NotesViewModel
import nz.ac.uclive.grb96.assignment1.R
import nz.ac.uclive.grb96.assignment1.model.note.NoteSection
import nz.ac.uclive.grb96.assignment1.util.getTimeText
import org.json.JSONArray
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import javax.net.ssl.HttpsURLConnection

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

        updateDay()

        val previousDayButton: Button = view.findViewById(R.id.previousDayButton)
        previousDayButton.setOnClickListener {
            date = date.minusDays(1)
            updateDay()
        }

        val nextDayButton: Button = view.findViewById(R.id.nextDayButton)
        nextDayButton.setOnClickListener {
            date = date.plusDays(1)
            updateDay()
        }

        return view
    }

    private fun updateDay() {
        updateText()
        getPublicHolidays()  // TODO: retrieve new public holidays only on country or year change
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

    private fun getPublicHolidays() {
        val publicHolidaysUrl = URL("https://date.nager.at/api/v3/PublicHolidays/${date.year}/NZ")  // TODO: make country selectable using /AvailableCountries endpoint
        lifecycleScope.launch {
            try {
                val result = getJsonArray(publicHolidaysUrl)
                for (index in 0 until result.length()) {
                    val publicHoliday = result.getJSONObject(index)
                    val publicHolidayDate = publicHoliday.getString("date").split("-")
                    if (date == LocalDate.of(publicHolidayDate[0].toInt(), publicHolidayDate[1].toInt(), publicHolidayDate[2].toInt())) {
                        Toast.makeText(requireContext(), publicHoliday.getString("name"), Toast.LENGTH_LONG).show()  // TODO: Add to text instead of toast message
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: public holiday searching unavailable.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun getJsonArray(url: URL): JSONArray {
        val result = withContext(Dispatchers.IO) {
            val connection = url.openConnection() as HttpsURLConnection
            try {
                val json = BufferedInputStream(connection.inputStream).readBytes()
                    .toString(Charset.defaultCharset())
                JSONArray(json)
            } finally {
                connection.disconnect()
            }
        }
        return result
    }
}
