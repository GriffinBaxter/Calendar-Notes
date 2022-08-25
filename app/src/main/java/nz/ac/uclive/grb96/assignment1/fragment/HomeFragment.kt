package nz.ac.uclive.grb96.assignment1.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import nz.ac.uclive.grb96.assignment1.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val calendarButton: Button = view.findViewById(R.id.calendarButton)
        calendarButton.setOnClickListener {
            calendar()
        }

        val notesButton: Button = view.findViewById(R.id.viewNotesButton)
        notesButton.setOnClickListener {
            notes()
        }

        return view
    }

    private fun notes() {
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_notesListFragment)
    }

    private fun calendar() {
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_calendarFragment)
    }
}
