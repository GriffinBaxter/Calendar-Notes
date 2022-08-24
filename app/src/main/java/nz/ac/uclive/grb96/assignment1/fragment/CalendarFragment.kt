package nz.ac.uclive.grb96.assignment1.fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.bold
import androidx.core.text.italic
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

    private val publicHolidaysApi = "https://date.nager.at/api/v3"
    private lateinit var countrySpinner: Spinner
    private val countryToCode = HashMap<String, String>()
    private var dateToPublicHoliday = HashMap<LocalDate, String>()
    private var publicHolidaysLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        countrySpinner = view.findViewById(R.id.countrySpinner)
        dayText = view.findViewById(R.id.dayText)
        calendarText = view.findViewById(R.id.calendarText)

        updateText()

        val previousDayButton: Button = view.findViewById(R.id.previousDayButton)
        previousDayButton.setOnClickListener {
            val oldDate = date
            val newDate = date.minusDays(1)
            date = newDate
            if (newDate.year != oldDate.year) {
                getPublicHolidays()
            }
            updateText()
        }

        val nextDayButton: Button = view.findViewById(R.id.nextDayButton)
        nextDayButton.setOnClickListener {
            val oldDate = date
            val newDate = date.plusDays(1)
            date = newDate
            if (newDate.year != oldDate.year) {
                getPublicHolidays()
            }
            updateText()
        }

        getCountries()

        return view
    }

    private fun updateText() {
        dayText.text = "${date.dayOfMonth} ${date.month} ${date.year}"
        if (!publicHolidaysLoaded) {
            updateCalendarDayText("Loading...")
        } else {
            updateCalendarDayText(dateToPublicHoliday[date])
        }
    }

    private fun updateCalendarDayText(publicHoliday: String?) {
        val calendarDayText = SpannableStringBuilder()

        val dueDateSection: NoteSection? = viewModel.getDueDateSectionFromDate(date)
        calendarDayText.bold { append("DUE\n") }
        if (dueDateSection == null) {
            calendarDayText.italic { append("Nothing due") }
        } else {
            calendarDayText.append(dueDateSection.content)
        }

        val eventSections: List<NoteSection> = viewModel.getEventSectionsFromDate(date)
        calendarDayText
            .bold { append("\n\nEVENTS\n") }
        if (eventSections.isEmpty()) {
            calendarDayText.italic { append("No events") }
        } else {
            val dateTimes = arrayListOf<LocalDateTime>()
            for (section: NoteSection in eventSections) {
                dateTimes.add(section.eventTime!!.getEventsLocalDateStartTime())
            }
            val sortedDateTimes = dateTimes.sorted()
            for (dateTime: LocalDateTime in sortedDateTimes) {
                for (section: NoteSection in eventSections) {
                    if (dateTime == section.eventTime!!.getEventsLocalDateStartTime()) {
                        calendarDayText
                            .italic { bold { append(getTimeText(section.eventTime.time)) } }
                            .append("\n")
                            .append(section.content)
                            .append("\n")
                        break
                    }
                }
            }
        }

        calendarDayText
            .bold { append("\n\nPUBLIC HOLIDAY\n") }
        if (publicHoliday == null) {
            calendarDayText.italic { append("No public holiday") }
        } else {
            calendarDayText.append(publicHoliday)
        }

        calendarText.text = calendarDayText
    }

    private fun getCountries() {
        var countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayListOf("Loading..."))
        countrySpinner.adapter = countryAdapter
        val publicHolidaysUrl = URL("${publicHolidaysApi}/AvailableCountries")
        val deviceCountryCode: String = requireContext().resources.configuration.locales.get(0).country
        var deviceCountryName: String? = null
        lifecycleScope.launch {
            try {
                val result = getJsonArray(publicHolidaysUrl)
                for (index in 0 until result.length()) {
                    val country = result.getJSONObject(index)
                    val name = country.getString("name")
                    val code = country.getString("countryCode")
                    countryToCode[name] = code
                    if (code == deviceCountryCode) {
                        deviceCountryName = name
                    }
                }
                val sortedCountries = countryToCode.keys.sorted()
                countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sortedCountries)
                countrySpinner.adapter = countryAdapter
                if (deviceCountryName != null) {
                    countrySpinner.setSelection(sortedCountries.indexOf(deviceCountryName))
                }
                setCountryListener()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: country searching unavailable.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setCountryListener() {
        val countryListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                getPublicHolidays()
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getPublicHolidays()
            }
        }
        countrySpinner.onItemSelectedListener = countryListener
    }

    private fun getPublicHolidays() {
        publicHolidaysLoaded = false
        dateToPublicHoliday = HashMap()
        updateText()
        val countryCode = countryToCode[countrySpinner.selectedItem.toString()]
        val publicHolidaysUrl = URL("${publicHolidaysApi}/PublicHolidays/${date.year}/${countryCode}")
        lifecycleScope.launch {
            try {
                val result = getJsonArray(publicHolidaysUrl)
                for (index in 0 until result.length()) {
                    val publicHoliday = result.getJSONObject(index)
                    val publicHolidayDateList = publicHoliday.getString("date").split("-")
                    val publicHolidayLocalDate = LocalDate.of(publicHolidayDateList[0].toInt(), publicHolidayDateList[1].toInt(), publicHolidayDateList[2].toInt())
                    val publicHolidayName = publicHoliday.getString("name")
                    dateToPublicHoliday[publicHolidayLocalDate] = publicHolidayName
                }
                publicHolidaysLoaded = true
                updateText()
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
