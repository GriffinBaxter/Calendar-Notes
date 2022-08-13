package nz.ac.uclive.grb96.assignment1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val view = inflater.inflate(R.layout.fragment_notes_list, container, false)

        val noteAdapter = NotesAdapter(viewModel.notes.value!!,this)
        viewModel.notes.observe(viewLifecycleOwner) { newNotes ->
            noteAdapter.setData(newNotes)
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
}