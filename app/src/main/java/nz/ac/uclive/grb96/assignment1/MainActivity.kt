package nz.ac.uclive.grb96.assignment1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if ("${applicationContext.packageName}.shortcuts.calendar" == intent.action) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val navController = navHostFragment?.findNavController()
            navController?.navigate(R.id.action_global_calendar)
        } else if ("${applicationContext.packageName}.shortcuts.notes" == intent.action) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val navController = navHostFragment?.findNavController()
            navController?.navigate(R.id.action_global_notes)
        }
    }
}
