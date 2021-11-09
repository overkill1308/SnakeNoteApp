package com.example.snakenoteapp.listeners;

import com.example.snakenoteapp.entities.Note;

public interface NotesListener {

    void onNoteClicked(Note note, int position);

}
