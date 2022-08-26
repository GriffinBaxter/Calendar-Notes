package nz.ac.uclive.grb96.calendarnotes.model.note

class Note (val name: String, val type: NoteType, val sections: MutableList<NoteSection>) {
    override fun toString() = name
}
