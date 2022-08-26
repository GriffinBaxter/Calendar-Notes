package nz.ac.uclive.grb96.calendarnotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nz.ac.uclive.grb96.calendarnotes.R
import nz.ac.uclive.grb96.calendarnotes.model.note.NoteSection

class SectionsAdapter(private var sections: List<NoteSection>, private val onSectionListener: OnSectionListener) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    class SectionViewHolder(itemView: View, val onSectionListener: OnSectionListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textView: TextView

        init {
            textView = itemView.findViewById(R.id.note_text)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onSectionListener.onSectionClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_list_item, parent, false)
        return SectionViewHolder(view, onSectionListener)
    }

    override fun onBindViewHolder(viewHolder: SectionViewHolder, position: Int) {
        viewHolder.textView.text = sections[position].toString()
    }

    override fun getItemCount() = sections.size

    interface OnSectionListener {
        fun onSectionClick(position: Int)
    }
}
