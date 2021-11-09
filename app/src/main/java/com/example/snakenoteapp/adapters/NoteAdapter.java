package com.example.snakenoteapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakenoteapp.R;
import com.example.snakenoteapp.entities.Note;
import com.example.snakenoteapp.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private List<Note> noteList;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> notesSource;

    public NoteAdapter(List<Note> noteList, NotesListener notesListener) {
        this.noteList = noteList;
        this.notesListener = notesListener;
        notesSource = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(noteList.get(position));
        holder.layout_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesListener.onNoteClicked(noteList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView tv_title, tv_subtitle, tv_date_time;
        LinearLayout layout_note;
        RoundedImageView img_note;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_subtitle = itemView.findViewById(R.id.tv_subtitle);
            tv_date_time = itemView.findViewById(R.id.tv_dateTime);
            layout_note = itemView.findViewById(R.id.layout_note);
            img_note = itemView.findViewById(R.id.img_note);

        }

        void setNote(Note note){
            tv_title.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()){
                tv_subtitle.setVisibility(View.GONE);
            } else {
                tv_subtitle.setText(note.getSubtitle());
            }
            tv_date_time.setText(note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) layout_note.getBackground();
            if (note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath() != null){
                img_note.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                img_note.setVisibility(View.VISIBLE);
            } else {
                img_note.setVisibility(View.GONE);
            }
        }

    }

    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()){
                    noteList = notesSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSource){
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                            || note.getNoteText().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    noteList = temp;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        }, 500);
    }

    public void cancelTimer(){
        if (timer != null){
            timer.cancel();
        }
    }
}
