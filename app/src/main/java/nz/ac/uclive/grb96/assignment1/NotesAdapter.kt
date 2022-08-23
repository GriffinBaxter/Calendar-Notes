package nz.ac.uclive.grb96.assignment1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nz.ac.uclive.grb96.assignment1.model.note.Note

class NotesAdapter(private var notes: List<Note>, private val onNoteListener: OnNoteListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View, val onNoteListener: OnNoteListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textView: TextView

        init {
            textView = itemView.findViewById(R.id.note_text)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onNoteListener.onNoteClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(view, onNoteListener)
    }

    override fun onBindViewHolder(viewHolder: NoteViewHolder, position: Int) {
        viewHolder.textView.text = notes[position].toString()
    }

    override fun getItemCount() = notes.size

    fun setData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    interface OnNoteListener {
        fun onNoteClick(position: Int)
    }
}
