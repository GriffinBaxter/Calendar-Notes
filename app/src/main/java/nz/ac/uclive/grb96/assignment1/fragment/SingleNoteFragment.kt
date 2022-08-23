package nz.ac.uclive.grb96.assignment1.fragment

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels
import nz.ac.uclive.grb96.assignment1.*
import nz.ac.uclive.grb96.assignment1.model.datetime.DateStartEndTime
import nz.ac.uclive.grb96.assignment1.model.datetime.StartEndTime
import nz.ac.uclive.grb96.assignment1.model.datetime.YearMonthDay
import nz.ac.uclive.grb96.assignment1.model.note.Note
import nz.ac.uclive.grb96.assignment1.model.note.NoteType
import nz.ac.uclive.grb96.assignment1.util.readData
import nz.ac.uclive.grb96.assignment1.util.writeData
import java.time.LocalDate
import java.time.LocalDateTime

class SingleNoteFragment : Fragment() {

    private val viewModel: NotesViewModel by activityViewModels()

    private lateinit var noteSections: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        readData(requireContext(), viewModel)

        val view = inflater.inflate(R.layout.fragment_single_note, container, false)
        val name = arguments?.getString("name")!!
        val note = viewModel.getNoteFromName(name)!!

        val noteName = view.findViewById<TextView>(R.id.note_name)
        noteName.text = note.name

        val newSectionButton: Button = view.findViewById(R.id.newSectionButton)
        newSectionButton.setOnClickListener {
            newSection(note)
        }

        val shareButton: Button = view.findViewById(R.id.shareButton)
        shareButton.setOnClickListener {
            val options = arrayOf("Email", "Text")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("How would you like to share your note?")
            builder.setItems(options) { _, optionId ->
                dispatchAction(optionId, note)
            }
            builder.show()
        }

        noteSections = view.findViewById(R.id.note_sections)
        updateNoteSections(note)

        return view
    }

    private fun dispatchAction(optionId: Int, note: Note) {
        when (optionId) {
            0 -> {
                val uri = Uri.parse("mailto:")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra(Intent.EXTRA_SUBJECT, note.name)
                intent.putExtra(Intent.EXTRA_TEXT, getNoteText(note).toString())
                startActivity(intent)
            }
            1 -> {
                val uri = Uri.parse("smsto:")
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra("sms_body", getNoteText(note).toString())
                startActivity(intent)
            }
        }
    }

    private fun newSection(note: Note) {
        val builder = AlertDialog.Builder(requireContext())
        when (note.type) {
            NoteType.STANDARD -> {
                newStandardSection(builder, note)
            }
            NoteType.DUE_DATES -> {
                newDueDatesSection(builder, note)
            }
            NoteType.EVENTS -> {
                newEventsSection(builder, note)
            }
        }
    }

    private fun newStandardSection(
        builder: AlertDialog.Builder,
        note: Note
    ) {
        val form = layoutInflater.inflate(R.layout.new_standard_section_dialog, null, false)
        builder.setView(form)

        val headerBox: EditText = form.findViewById(R.id.headerBox)
        val contentBox: EditText = form.findViewById(R.id.contentBox)

        builder.setNegativeButton("Cancel", null)

        builder.setPositiveButton("Add") { _, _ ->
            viewModel.addNoteSection(
                note,
                NoteSection(contentBox.text.toString(), header = headerBox.text.toString())
            )
            writeData(requireActivity(), viewModel.notes.value!!)
            updateNoteSections(note)
        }

        builder.show()
    }

    private fun newDueDatesSection(
        builder: AlertDialog.Builder,
        note: Note
    ) {
        val form = layoutInflater.inflate(R.layout.new_due_dates_section_dialog, null, false)
        builder.setView(form)

        val dueDatePicker: DatePicker = form.findViewById(R.id.dueDatePicker)
        val contentBox: EditText = form.findViewById(R.id.contentBox)

        builder.setNegativeButton("Cancel", null)

        builder.setPositiveButton("Add") { _, _ ->
            viewModel.addNoteSection(
                note,
                NoteSection(contentBox.text.toString(), dueDate = YearMonthDay(dueDatePicker.year, dueDatePicker.month, dueDatePicker.dayOfMonth))
            )
            writeData(requireActivity(), viewModel.notes.value!!)
            updateNoteSections(note)
        }

        builder.show()
    }

    private fun newEventsSection(
        builder: AlertDialog.Builder,
        note: Note
    ) {
        val form = layoutInflater.inflate(R.layout.new_events_section_dialog, null, false)
        builder.setView(form)

        val datePicker: DatePicker = form.findViewById(R.id.datePicker)
        val contentBox: EditText = form.findViewById(R.id.contentBox)
        val timeText: TextView = form.findViewById(R.id.timeText)

        val startEndTime = StartEndTime(12, 0, 13, 0)

        timeText.text = getTimeText(startEndTime)

        val startTimeButton: Button = form.findViewById(R.id.startTimeButton)
        startTimeButton.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    startEndTime.startHour = hour
                    startEndTime.startMinute = minute
                    timeText.text = getTimeText(startEndTime)
                },
                startEndTime.startHour,
                startEndTime.startMinute,
                false,
            )
            timePicker.show()
        }
        val endTimeButton: Button = form.findViewById(R.id.endTimeButton)
        endTimeButton.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    startEndTime.endHour = hour
                    startEndTime.endMinute = minute
                    timeText.text = getTimeText(startEndTime)
                },
                startEndTime.endHour,
                startEndTime.endMinute,
                false,
            )
            timePicker.show()
        }

        builder.setNegativeButton("Cancel", null)

        builder.setPositiveButton("Add", null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val eventTime = DateStartEndTime(YearMonthDay(datePicker.year, datePicker.month, datePicker.dayOfMonth), startEndTime)
                    val noteSection = NoteSection(contentBox.text.toString(), eventTime = eventTime)
                    if (noteSection.eventTime!!.time.getEventsLocalStartTime() < noteSection.eventTime.time.getEventsLocalEndTime()) {
                        viewModel.addNoteSection(
                            note,
                            noteSection
                        )
                        writeData(requireActivity(), viewModel.notes.value!!)
                        updateNoteSections(note)
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Sorry, the start time must be before the end time. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }

    private fun updateNoteSections(note: Note) {
        val noteText = getNoteText(note)
        noteSections.text = noteText
    }

    private fun getNoteText(note: Note): SpannableStringBuilder {
        val noteText = SpannableStringBuilder()
        when (note.type) {
            NoteType.STANDARD -> {
                for (section: NoteSection in note.sections) {
                    noteText
                        .bold { append(section.header) }
                        .append("\n")
                        .append(section.content)
                        .append("\n\n")
                }
            }
            NoteType.DUE_DATES -> {
                val dates = arrayListOf<LocalDate>()
                for (section: NoteSection in note.sections) {
                    dates.add(section.getDueDatesLocalDate())
                }
                val sortedDates = dates.sorted()
                for (date: LocalDate in sortedDates) {
                    for (section: NoteSection in note.sections) {
                        if (date == section.getDueDatesLocalDate()) {
                            noteText
                                .bold { append(date.dayOfMonth.toString()) }
                                .append(" ")
                                .bold { append(date.month.name) }
                                .append(" ")
                                .bold { append(date.year.toString()) }
                                .append("\n")
                                .append(section.content)
                                .append("\n\n")
                            break
                        }
                    }
                }
            }
            NoteType.EVENTS -> {
                val dateTimes = arrayListOf<LocalDateTime>()
                for (section: NoteSection in note.sections) {
                    dateTimes.add(section.eventTime!!.getEventsLocalDateStartTime())
                }
                val sortedDateTimes = dateTimes.sorted()
                for (dateTime: LocalDateTime in sortedDateTimes) {
                    for (section: NoteSection in note.sections) {
                        if (dateTime == section.eventTime!!.getEventsLocalDateStartTime()) {
                            noteText
                                .bold { append(dateTime.dayOfMonth.toString()) }
                                .append(" ")
                                .bold { append(dateTime.month.name) }
                                .append(" ")
                                .bold { append(dateTime.year.toString()) }
                                .bold { append(" (") }
                                .bold { append(getTimeText(section.eventTime.time)) }
                                .bold { append(")") }
                                .append("\n")
                                .append(section.content)
                                .append("\n\n")
                            break
                        }
                    }
                }
            }
        }
        return noteText
    }

    private fun getTimeText(startEndTime: StartEndTime): String {
        return startEndTime.getEventsLocalStartTime().toString() + " - " + startEndTime.getEventsLocalEndTime().toString()
    }
}
