package nz.ac.uclive.grb96.calendarnotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleShortcuts()
    }

    private fun handleShortcuts() {
        if ("${applicationContext.packageName}.shortcuts.calendar" == intent.action) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val navController = navHostFragment?.findNavController()
            navController?.navigate(R.id.action_global_calendar)
        } else if ("${applicationContext.packageName}.shortcuts.notes" == intent.action) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val navController = navHostFragment?.findNavController()
            navController?.navigate(R.id.action_global_notes)
        } else if (intent.action?.startsWith("${applicationContext.packageName}.shortcuts.note.") == true) {
            val noteName = intent.action!!.split(".").last()
            val args = bundleOf("name" to noteName)
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val navController = navHostFragment?.findNavController()
            navController?.navigate(R.id.action_global_notes)
            try {
                navController?.navigate(R.id.action_notesListFragment_to_singleNoteFragment, args)
            } catch (e: Exception) {}
        }
    }
}
