package nz.ac.uclive.grb96.assignment1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

    fun addNoteSection(note: Note, noteSection: NoteSection) {
        note.sections.add(noteSection)
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

    fun setNotes(notes: MutableList<Note>) {
        _notes.value = notes
        _notes.notifyObserver()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}