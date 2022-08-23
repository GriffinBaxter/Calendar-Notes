package nz.ac.uclive.grb96.assignment1.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import nz.ac.uclive.grb96.assignment1.*
import nz.ac.uclive.grb96.assignment1.model.note.Note
import nz.ac.uclive.grb96.assignment1.model.note.NoteType
import nz.ac.uclive.grb96.assignment1.util.readData
import nz.ac.uclive.grb96.assignment1.util.writeData

class NotesListFragment : Fragment(), NotesAdapter.OnNoteListener {

    private val viewModel: NotesViewModel by activityViewModels()

    private var deleteMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        readData(requireContext(), viewModel)

        val view = inflater.inflate(R.layout.fragment_notes_list, container, false)

        val noteAdapter = NotesAdapter(viewModel.notes.value!!,this)
        viewModel.notes.observe(viewLifecycleOwner) { newNotes ->
            noteAdapter.setData(newNotes)
        }

        val newNoteButton: Button = view.findViewById(R.id.newNoteButton)
        newNoteButton.setOnClickListener {
            newNote()
        }

        val deleteModeSwitch: SwitchMaterial = view.findViewById(R.id.deleteModeSwitch)
        deleteModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                deleteMode = true
                Toast.makeText(requireContext(), "Warning: delete mode will cause any selected note to be deleted.", Toast.LENGTH_LONG).show()
            } else {
                deleteMode = false
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = noteAdapter
        return view
    }

    override fun onNoteClick(position: Int) {
        val note = viewModel.notes.value!![position]
        if (deleteMode) {
            viewModel.deleteNote(note)
            Toast.makeText(requireContext(), "Successfully deleted note.", Toast.LENGTH_LONG).show()
            writeData(requireActivity(), viewModel.notes.value!!)
        } else {
            val args = bundleOf("name" to note.name)
            Navigation.findNavController(requireView()).navigate(R.id.action_notesListFragment_to_singleNoteFragment, args)
        }
    }

    private fun newNote() {
        val builder = AlertDialog.Builder(requireContext())

        val form = layoutInflater.inflate(R.layout.new_note_dialog, null, false)
        builder.setView(form)

        val nameBox: EditText = form.findViewById(R.id.nameBox)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayListOf("Standard", "Due Dates", "Events"))
        val noteTypeSpinner: Spinner = form.findViewById(R.id.noteTypeSpinner)
        noteTypeSpinner.adapter = adapter

        builder.setNegativeButton("Cancel", null)

        builder.setPositiveButton("Add", null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val noteType: NoteType = if (noteTypeSpinner.selectedItem.toString() == "Standard") {
                        NoteType.STANDARD
                    } else if (noteTypeSpinner.selectedItem.toString() == "Due Dates") {
                        NoteType.DUE_DATES
                    } else {
                        NoteType.EVENTS
                    }

                    if (viewModel.getNoteFromName(nameBox.text.toString()) == null) {
                        viewModel.addNote(Note(nameBox.text.toString(), noteType, arrayListOf()))
                        writeData(requireActivity(), viewModel.notes.value!!)
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Sorry, a note with the name \"${nameBox.text}\" already exists. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }
}