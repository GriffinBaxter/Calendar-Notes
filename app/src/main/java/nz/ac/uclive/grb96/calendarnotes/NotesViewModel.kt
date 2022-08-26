package nz.ac.uclive.grb96.calendarnotes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nz.ac.uclive.grb96.calendarnotes.model.datetime.DateStartEndTime
import nz.ac.uclive.grb96.calendarnotes.model.datetime.YearMonthDay
import nz.ac.uclive.grb96.calendarnotes.model.note.Note
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteSection
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteType
import java.time.LocalDate

class NotesViewModel: ViewModel() {

    private var _notes = MutableLiveData<MutableList<Note>>(arrayListOf())
    val notes: LiveData<MutableList<Note>>
        get() = _notes

    private var _numNotes = MutableLiveData<Int>(notes.value!!.size)
    val numNotes: LiveData<Int>
        get() = _numNotes

    fun addNote(note: Note) {
        _notes.value?.add(note)
        _notes.notifyObserver()
        _numNotes.value = _notes.value!!.size
    }

    fun deleteNote(note: Note) {
        _notes.value?.remove(note)
        _notes.notifyObserver()
        _numNotes.value = _notes.value!!.size
    }

    fun addNoteSection(note: Note, noteSection: NoteSection) {
        note.sections.add(noteSection)
        _notes.notifyObserver()
    }

    fun deleteNoteSection(note: Note, noteSection: NoteSection) {
        note.sections.remove(noteSection)
        _notes.notifyObserver()
    }

    fun getNoteFromName(name: String): Note? {
        for (note: Note in _notes.value!!) {
            if (note.name == name) {
                return note
            }
        }
        return null
    }

    fun getNoteFromHeader(header: String): Note? {
        for (note: Note in _notes.value!!) {
            if (note.type == NoteType.STANDARD) {
                for (section: NoteSection in note.sections) {
                    if (section.header!! == header) {
                        return note
                    }
                }
            }
        }
        return null
    }

    fun getNoteFromDueDate(dueDate: YearMonthDay): Note? {
        for (note: Note in _notes.value!!) {
            if (note.type == NoteType.DUE_DATES) {
                for (section: NoteSection in note.sections) {
                    if (section.dueDate!! == dueDate) {
                        return note
                    }
                }
            }
        }
        return null
    }

    fun getOverlappingNoteFromEventTime(eventTime: DateStartEndTime): Note? {
        for (note: Note in _notes.value!!) {
            if (note.type == NoteType.EVENTS) {
                for (section: NoteSection in note.sections) {
                    val sectionStartTime = section.eventTime!!.time.getEventsLocalStartTime()
                    val sectionEndTime = section.eventTime.time.getEventsLocalEndTime()
                    val givenStartTime = eventTime.time.getEventsLocalStartTime()
                    val givenEndTime = eventTime.time.getEventsLocalEndTime()
                    if (
                        (section.eventTime.date == eventTime.date) &&
                        (sectionStartTime < givenEndTime) && (givenStartTime < sectionEndTime)
                    ) {
                        return note
                    }
                }
            }
        }
        return null
    }

    fun getDueDateSectionFromDate(date: LocalDate): NoteSection? {
        for (note: Note in _notes.value!!) {
            if (note.type == NoteType.DUE_DATES) {
                for (section: NoteSection in note.sections) {
                    if (section.dueDate!! == YearMonthDay(date.year, date.month.value - 1, date.dayOfMonth)) {
                        return section
                    }
                }
            }
        }
        return null
    }

    fun getEventSectionsFromDate(date: LocalDate): List<NoteSection> {
        val sections = arrayListOf<NoteSection>()
        for (note: Note in _notes.value!!) {
            if (note.type == NoteType.EVENTS) {
                for (section: NoteSection in note.sections) {
                    if (section.eventTime!!.date == YearMonthDay(date.year, date.month.value - 1, date.dayOfMonth)) {
                        sections.add(section)
                    }
                }
            }
        }
        return sections
    }

    fun setNotes(notes: MutableList<Note>) {
        _notes.value = notes
        _notes.notifyObserver()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}