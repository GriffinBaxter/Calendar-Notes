package nz.ac.uclive.grb96.assignment1

import android.content.Intent
import android.net.Uri
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
        Persistence.readData(requireContext(), viewModel)

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

        val form = layoutInflater.inflate(R.layout.new_section_dialog, null, false)
        builder.setView(form)

        val headerBox: EditText = form.findViewById(R.id.headerBox)
        val contentBox: EditText = form.findViewById(R.id.contentBox)

        builder.setPositiveButton("Add") { _, _ ->
            viewModel.addNoteSection(note, NoteSection(headerBox.text.toString(), contentBox.text.toString()))
            Persistence.writeData(requireActivity(), viewModel.notes.value!!)
            updateNoteSections(note)
        }

        builder.show()
    }

    private fun updateNoteSections(note: Note) {
        val noteText = getNoteText(note)
        noteSections.text = noteText
    }

    private fun getNoteText(note: Note): SpannableStringBuilder {
        val noteText = SpannableStringBuilder()
        for (section: NoteSection in note.sections) {
            noteText
                .bold { append(section.header) }
                .append("\n")
                .append(section.content)
                .append("\n\n")
        }
        return noteText
    }
}
