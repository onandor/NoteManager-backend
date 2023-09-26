package com.onandor.data

enum class NoteLocation(val value: Int) {
    NOTES(0),
    ARCHIVE(1),
    TRASH(2),
    ALL(3);

    companion object {
        fun fromInt(value: Int) = NoteLocation.values().first { it.value == value }
    }
}