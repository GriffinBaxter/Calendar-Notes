package nz.ac.uclive.grb96.calendarnotes.fragment

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import nz.ac.uclive.grb96.calendarnotes.*
import nz.ac.uclive.grb96.calendarnotes.model.note.Note
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteType
import nz.ac.uclive.grb96.calendarnotes.util.readData
import nz.ac.uclive.grb96.calendarnotes.util.removeWhitespaceAndTrim
import nz.ac.uclive.grb96.calendarnotes.util.writeData
import nz.ac.uclive.grb96.calendarnotes.NotesAdapter
import nz.ac.uclive.grb96.calendarnotes.NotesViewModel

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
                Toast.makeText(requireContext(), resources.getString(R.string.warning_delete_mode), Toast.LENGTH_LONG).show()
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
            removeShortcut(note.name)
            Toast.makeText(requireContext(), resources.getString(R.string.success_delete_note), Toast.LENGTH_SHORT).show()
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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayListOf(
                resources.getString(R.string.standard),
                resources.getString(R.string.due_dates),
                resources.getString(R.string.events),
            )
        )
        val noteTypeSpinner: Spinner = form.findViewById(R.id.noteTypeSpinner)
        noteTypeSpinner.adapter = adapter

        builder.setNegativeButton(resources.getString(R.string.cancel), null)

        builder.setPositiveButton(resources.getString(R.string.add), null).create().apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val noteType: NoteType = if (noteTypeSpinner.selectedItem.toString() == resources.getString(R.string.standard)) {
                        NoteType.STANDARD
                    } else if (noteTypeSpinner.selectedItem.toString() == resources.getString(R.string.due_dates)) {
                        NoteType.DUE_DATES
                    } else {
                        NoteType.EVENTS
                    }

                    val name = removeWhitespaceAndTrim(nameBox.text.toString())

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_note_name_empty), Toast.LENGTH_LONG).show()
                    } else if (viewModel.getNoteFromName(name) == null) {
                        viewModel.addNote(Note(name, noteType, arrayListOf()))
                        pushShortcut(name)
                        writeData(requireActivity(), viewModel.notes.value!!)
                        dismiss()
                    } else {
                        Toast.makeText(requireContext(), resources.getString(R.string.sorry_note_name_exists, name), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.show()
    }

    private fun pushShortcut(name: String) {
        val intent = Intent()
        intent.action = "${requireContext().packageName}.shortcuts.note.$name"
        intent.component = ComponentName(requireContext().packageName, "${requireContext().packageName}.MainActivity")
        val shortcut = ShortcutInfoCompat.Builder(requireContext(), "note:$name")
            .setShortLabel(resources.getString(R.string.view_note, name))
            .setIcon(IconCompat.createWithResource(requireContext(), R.drawable.ic_baseline_note_48))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(requireContext(), shortcut)
    }

    private fun removeShortcut(name: String) {
        ShortcutManagerCompat.removeDynamicShortcuts(requireContext(), listOf("note:$name"))
    }

}
