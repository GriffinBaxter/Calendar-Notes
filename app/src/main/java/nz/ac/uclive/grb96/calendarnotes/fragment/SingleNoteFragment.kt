package nz.ac.uclive.grb96.calendarnotes.fragment

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
import androidx.core.text.italic
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import nz.ac.uclive.grb96.calendarnotes.*
import nz.ac.uclive.grb96.calendarnotes.model.datetime.DateStartEndTime
import nz.ac.uclive.grb96.calendarnotes.model.datetime.StartEndTime
import nz.ac.uclive.grb96.calendarnotes.model.datetime.YearMonthDay
import nz.ac.uclive.grb96.calendarnotes.model.note.Note
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteSection
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteType
import nz.ac.uclive.grb96.calendarnotes.util.getTimeText
import nz.ac.uclive.grb96.calendarnotes.util.readData
import nz.ac.uclive.grb96.calendarnotes.util.removeWhitespaceAndTrim
import nz.ac.uclive.grb96.calendarnotes.util.writeData
import nz.ac.uclive.grb96.calendarnotes.NotesViewModel
import nz.ac.uclive.grb96.calendarnotes.SectionsAdapter
import java.time.LocalDate
import java.time.LocalDateTime

class SingleNoteFragment : Fragment(), SectionsAdapter.OnSectionListener {

    private val viewModel: NotesViewModel by activityViewModels()

    private lateinit var note: Note
    private lateinit var deleteSectionAlertDialog: AlertDialog

    private lateinit var noteSections: TextView

    private lateinit var sectionsInOrder: List<NoteSection>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        readData(requireContext(), viewModel)

        val view = inflater.inflate(R.layout.fragment_single_note, container, false)
        val name = arguments?.getString("name")!!

        val findNote = viewModel.getNoteFromName(name)
        if (findNote == null) {
            Toast.makeText(requireContext(), resources.getString(R.string.no_longer_exists), Toast.LENGTH_LONG).show()
        } else {
            note = findNote

            val noteName = view.findViewById<TextView>(R.id.note_name)
            noteName.text = note.name

            val newSectionButton: Button = view.findViewById(R.id.newSectionButton)
            newSectionButton.setOnClickListener {
                newSection(note)
            }

            val deleteSectionButton: Button = view.findViewById(R.id.deleteSectionButton)
            deleteSectionButton.setOnClickListener {
                if (note.sections.isEmpty()) {
                    Toast.makeText(requireContext(), resources.getString(R.string.no_sections_in_note), Toast.LENGTH_LONG).show()
                } else {
                    val builder = AlertDialog.Builder(requireContext())
                    val form = layoutInflater.inflate(R.layout.delete_sections_list, null, false)
                    builder.setView(form)

                    sectionsInOrder = getSectionsOrder(note.type, note.sections)
                    val sectionAdapter = SectionsAdapter(sectionsInOrder,this)

                    val recyclerView: RecyclerView = form.findViewById(R.id.sections_recycler_view)
                    recyclerView.adapter = sectionAdapter

                    deleteSectionAlertDialog = builder.show()
                }
            }

            val shareButton: Button = view.findViewById(R.id.shareButton)
            shareButton.setOnClickListener {
                val options = arrayOf(resources.getString(R.string.email), resources.getString(R.string.text))
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(resources.getString(R.string.how_sharing_note))
                builder.setItems(options) { _, optionId ->
                    dispatchAction(optionId, note)
                }
                builder.show()
            }

            noteSections = view.findViewById(R.id.note_sections)
            updateNoteSections(note)
        }
        return view
    }

    override fun onSectionClick(position: Int) {
        val section = sectionsInOrder[position]
        viewModel.deleteNoteSection(note, section)
        Toast.makeText(requireContext(), resources.getString(R.string.success_delete_section), Toast.LENGTH_SHORT).show()
        writeData(requireActivity(), viewModel.notes.value!!)
        updateNoteSections(note)
        deleteSectionAlertDialog.dismiss()
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

        builder.setNegativeButton(resources.getString(R.string.cancel), null)

        builder.setPositiveButton(resources.getString(R.string.add), null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val header = removeWhitespaceAndTrim(headerBox.text.toString())
                    val content = removeWhitespaceAndTrim(contentBox.text.toString())
                    if (header.isEmpty()) {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_section_header_empty), Toast.LENGTH_LONG).show()
                    } else if (content.isEmpty()) {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_section_content_empty), Toast.LENGTH_LONG).show()
                    } else if (viewModel.getNoteFromHeader(header) == null) {
                        viewModel.addNoteSection(
                            note,
                            NoteSection(content, header = header)
                        )
                        writeData(requireActivity(), viewModel.notes.value!!)
                        updateNoteSections(note)
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_note_section_header_exists), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }

    private fun newDueDatesSection(
        builder: AlertDialog.Builder,
        note: Note
    ) {
        val form = layoutInflater.inflate(R.layout.new_due_dates_section_dialog, null, false)
        builder.setView(form)

        val dueDatePicker: DatePicker = form.findViewById(R.id.dueDatePicker)
        var dueDate = YearMonthDay(dueDatePicker.year, dueDatePicker.month, dueDatePicker.dayOfMonth)
        dueDatePicker.setOnDateChangedListener { _, _, _, _ ->
            dueDate = YearMonthDay(dueDatePicker.year, dueDatePicker.month, dueDatePicker.dayOfMonth)
        }

        val contentBox: EditText = form.findViewById(R.id.contentBox)

        builder.setNegativeButton(resources.getString(R.string.cancel), null)

        builder.setPositiveButton(resources.getString(R.string.add), null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val content = removeWhitespaceAndTrim(contentBox.text.toString())
                    if (content.isEmpty()) {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_section_content_empty), Toast.LENGTH_LONG).show()
                    } else if (viewModel.getNoteFromDueDate(dueDate) == null) {
                        viewModel.addNoteSection(
                            note,
                            NoteSection(content, dueDate = dueDate)
                        )
                        writeData(requireActivity(), viewModel.notes.value!!)
                        updateNoteSections(note)
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_note_section_due_date_exists), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }

    private fun newEventsSection(
        builder: AlertDialog.Builder,
        note: Note
    ) {
        val form = layoutInflater.inflate(R.layout.new_events_section_dialog, null, false)
        builder.setView(form)

        val datePicker: DatePicker = form.findViewById(R.id.datePicker)
        var date = YearMonthDay(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        datePicker.setOnDateChangedListener { _, _, _, _ ->
            date = YearMonthDay(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        }

        val contentBox: EditText = form.findViewById(R.id.contentBox)
        val timeText: TextView = form.findViewById(R.id.timeText)

        val startEndTime = StartEndTime(10, 0, 11, 0)

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

        builder.setNegativeButton(resources.getString(R.string.cancel), null)

        builder.setPositiveButton(resources.getString(R.string.add), null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val eventTime = DateStartEndTime(date, startEndTime)
                    if (viewModel.getOverlappingNoteFromEventTime(eventTime) == null) {
                        val content = removeWhitespaceAndTrim(contentBox.text.toString())
                        val noteSection = NoteSection(content, eventTime = eventTime)
                        if (content.isEmpty()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.sorry_section_content_empty), Toast.LENGTH_LONG).show()
                        } else if (noteSection.eventTime!!.time.getEventsLocalStartTime() < noteSection.eventTime.time.getEventsLocalEndTime()) {
                            viewModel.addNoteSection(
                                note,
                                noteSection
                            )
                            writeData(requireActivity(), viewModel.notes.value!!)
                            updateNoteSections(note)
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), resources.getString(R.string.sorry_start_time_before), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_note_section_event_overlaps), Toast.LENGTH_LONG).show()
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
        if (note.sections.isEmpty()) {
            noteText.italic { append(resources.getString(R.string.no_sections)) }
        } else {
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
        }
        return noteText
    }

    private fun getSectionsOrder(type: NoteType, sections: List<NoteSection>): List<NoteSection> {
        val sectionsInOrder = arrayListOf<NoteSection>()
        when (type) {
            NoteType.STANDARD -> {
                return sections
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
                            sectionsInOrder.add(section)
                            break
                        }
                    }
                }
                return sectionsInOrder
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
                            sectionsInOrder.add(section)
                            break
                        }
                    }
                }
            }
        }
        return sectionsInOrder
    }
}
