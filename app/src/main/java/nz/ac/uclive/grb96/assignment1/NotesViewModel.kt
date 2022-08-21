package nz.ac.uclive.grb96.assignment1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotesViewModel: ViewModel() {

    private var _notes = MutableLiveData<MutableList<Note>>(arrayListOf(
        Note(
            "Uni To-Do",
            NoteType.DUE_DATES,
            arrayListOf(NoteSection("1 Oct", "Content...")),
        ),
        Note(
            "Schedule",
            NoteType.EVENTS,
            arrayListOf(NoteSection("1 Nov", "More Content...")),
        ),
    ))
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

    fun addNoteSection(note: Note, noteSection: NoteSection) {
        note.sections.add(noteSection)
        _notes.notifyObserver()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}