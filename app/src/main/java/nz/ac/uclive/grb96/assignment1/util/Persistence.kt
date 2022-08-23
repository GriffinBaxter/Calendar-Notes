package nz.ac.uclive.grb96.assignment1.util

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import nz.ac.uclive.grb96.assignment1.NotesViewModel
import nz.ac.uclive.grb96.assignment1.model.note.AllNotes
import nz.ac.uclive.grb96.assignment1.model.note.Note
import java.io.File
import java.io.FileNotFoundException

fun writeData(activity: FragmentActivity, notes: MutableList<Note>) {
    activity.openFileOutput("data.json", Context.MODE_PRIVATE).use {
        it?.write(Gson().toJson(AllNotes(notes)).toByteArray())
    }
}

fun readData(context: Context, viewModel: NotesViewModel) {
    val file = File(context.filesDir, "data.json")
    try {
        val contents = file.readText()
        val allNotes = Gson().fromJson(contents, AllNotes::class.java)
        viewModel.setNotes(allNotes.notes)
    } catch (e: FileNotFoundException) {}
}
