package nz.ac.uclive.grb96.assignment1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SingleNoteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_note, container, false)
        val noteName = view.findViewById<TextView>(R.id.note_name)
        val name = arguments?.getString("name")!!
        noteName.text = name
        return view
    }

}