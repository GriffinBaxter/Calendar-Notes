package nz.ac.uclive.grb96.assignment1

class Note (val name: String, val type: NoteType, val content: List<NoteSection>) {
    override fun toString() = name
}
