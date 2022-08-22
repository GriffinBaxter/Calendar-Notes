package nz.ac.uclive.grb96.assignment1

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

class NotesListFragment : Fragment(), NotesAdapter.OnNoteListener {

    private val viewModel: NotesViewModel by activityViewModels()

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

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = noteAdapter
        return view
    }

    override fun onNoteClick(position: Int) {
        val note = viewModel.notes.value!![position]
        val args = bundleOf("name" to note.name)
        Navigation.findNavController(requireView()).navigate(R.id.action_notesListFragment_to_singleNoteFragment, args)
    }

    private fun newNote() {
        val builder = AlertDialog.Builder(requireContext())

        val form = layoutInflater.inflate(R.layout.new_note_dialog, null, false)
        builder.setView(form)

        val nameBox: EditText = form.findViewById(R.id.nameBox)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayListOf("Standard", "Due Dates", "Events"))
        val noteTypeSpinner: Spinner = form.findViewById(R.id.noteTypeSpinner)
        noteTypeSpinner.adapter = adapter

        builder.setPositiveButton("Add") { _, _ ->
            val noteType: NoteType = if (noteTypeSpinner.selectedItem.toString() == "Standard") {
                NoteType.STANDARD
            } else if (noteTypeSpinner.selectedItem.toString() == "Due Dates") {
                NoteType.DUE_DATES
            } else {
                NoteType.EVENTS
            }

            if (viewModel.getNoteFromName(nameBox.text.toString()) != null) {
                Toast.makeText(requireContext(), "Unable to add note, a note with the name \"${nameBox.text}\" already exists. Please try again.", Toast.LENGTH_LONG).show()
            } else {
                viewModel.addNote(Note(nameBox.text.toString(), noteType, arrayListOf()))
                writeData(requireActivity(), viewModel.notes.value!!)
            }
        }

        builder.show()
    }
}