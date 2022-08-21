package nz.ac.uclive.grb96.assignment1

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import androidx.fragment.app.activityViewModels

class SingleNoteFragment : Fragment() {

    private val viewModel: NotesViewModel by activityViewModels()

    private lateinit var noteSections: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_note, container, false)
        val name = arguments?.getString("name")!!
        val note = viewModel.getNoteFromName(name)!!

        val noteName = view.findViewById<TextView>(R.id.note_name)
        noteName.text = note.name

        val newSectionButton: Button = view.findViewById(R.id.newSectionButton)
        newSectionButton.setOnClickListener {
            newSection(note)
        }

        noteSections = view.findViewById(R.id.note_sections)
        updateNoteSections(note)

        return view
    }

    private fun newSection(note: Note) {
        val builder = AlertDialog.Builder(requireContext())

        val form = layoutInflater.inflate(R.layout.new_section_dialog, null, false)
        builder.setView(form)

        val headerBox: EditText = form.findViewById(R.id.headerBox)
        val contentBox: EditText = form.findViewById(R.id.contentBox)

        builder.setPositiveButton("Add") { _, _ ->
            viewModel.addNoteSection(note, NoteSection(headerBox.text.toString(), contentBox.text.toString()))
            updateNoteSections(note)
        }

        builder.show()
    }

    private fun updateNoteSections(note: Note) {
        val noteText = SpannableStringBuilder()
        for (section: NoteSection in note.sections) {
            noteText
                .bold { append(section.header) }
                .append("\n")
                .append(section.content)
                .append("\n\n")
        }

        noteSections.text = noteText
    }

}
