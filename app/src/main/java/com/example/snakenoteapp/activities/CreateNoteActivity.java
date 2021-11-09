package com.example.snakenoteapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Insert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakenoteapp.R;
import com.example.snakenoteapp.database.NotesDatabase;
import com.example.snakenoteapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText input_note_title, input_note_subtitle, input_note_text;
    private TextView tv_date_time, tv_web_url;
    private LinearLayout layout_web_url;
    private ImageView image_note;
    private View view_subtitle_indicator;
    private String seclectedNoteColor;
    private String selectImagePath;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogAddUrl;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvailableNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        input_note_title = findViewById(R.id.input_note_title);
        input_note_subtitle = findViewById(R.id.input_note_subtitle);
        input_note_text = findViewById(R.id.input_note_text);
        tv_date_time = findViewById(R.id.tv_date_time);
        image_note = findViewById(R.id.image_note);
        tv_web_url = findViewById(R.id.tv_web_url);
        layout_web_url = findViewById(R.id.layout_web_url);
        view_subtitle_indicator = findViewById(R.id.view_subtitle_indicator);
        tv_date_time.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));
        ImageView image_back = findViewById(R.id.image_back);
        ImageView image_done = findViewById(R.id.image_done);

        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        image_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        seclectedNoteColor = "#333333";
        selectImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.img_remove_web_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_web_url.setText(null);
                layout_web_url.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.img_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_note.setImageBitmap(null);
                image_note.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectImagePath = "";
            }
        });

        if (getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null){
                if (type.equals("image")){
                    selectImagePath = getIntent().getStringExtra("imagePath");
                    image_note.setImageBitmap(BitmapFactory.decodeFile(selectImagePath));
                    image_note.setVisibility(View.VISIBLE);
                    findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
                } else if (type.equals("URL")){
                    tv_web_url.setText(getIntent().getStringExtra("URL"));
                    layout_web_url.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicatorColor();

    }

    private void setViewOrUpdateNote(){
        input_note_title.setText(alreadyAvailableNote.getTitle());
        input_note_subtitle.setText(alreadyAvailableNote.getSubtitle());
        input_note_text.setText(alreadyAvailableNote.getNoteText());
        tv_date_time.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()){
            image_note.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            image_note.setVisibility(View.VISIBLE);
            findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
            selectImagePath = alreadyAvailableNote.getImagePath();
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()){
            tv_web_url.setText(alreadyAvailableNote.getWebLink());
            layout_web_url.setVisibility(View.VISIBLE);
            Log.e("TAG", alreadyAvailableNote.getWebLink());
        }
    }

    private void saveNote(){
        if (input_note_title.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (input_note_subtitle.getText().toString().trim().isEmpty() && input_note_text.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Not can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(input_note_title.getText().toString());
        note.setSubtitle(input_note_subtitle.getText().toString());
        note.setNoteText(input_note_text.getText().toString());
        note.setDateTime(tv_date_time.getText().toString());
        note.setColor(seclectedNoteColor);
        note.setImagePath(selectImagePath);

        if (layout_web_url.getVisibility() == View.VISIBLE){
            note.setWebLink(tv_web_url.getText().toString());
        }

        if (alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.tv_miscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView img_color1 = layoutMiscellaneous.findViewById(R.id.img_color1);
        final ImageView img_color2 = layoutMiscellaneous.findViewById(R.id.img_color2);
        final ImageView img_color3 = layoutMiscellaneous.findViewById(R.id.img_color3);
        final ImageView img_color4 = layoutMiscellaneous.findViewById(R.id.img_color4);
        final ImageView img_color5 = layoutMiscellaneous.findViewById(R.id.img_color5);

        layoutMiscellaneous.findViewById(R.id.view_color1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seclectedNoteColor = "#333333";
                img_color1.setImageResource(R.drawable.ic_done);
                img_color2.setImageResource(0);
                img_color3.setImageResource(0);
                img_color4.setImageResource(0);
                img_color5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seclectedNoteColor = "#FDBE3B";
                img_color1.setImageResource(0);
                img_color2.setImageResource(R.drawable.ic_done);
                img_color3.setImageResource(0);
                img_color4.setImageResource(0);
                img_color5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seclectedNoteColor = "#FF4842";
                img_color1.setImageResource(0);
                img_color2.setImageResource(0);
                img_color3.setImageResource(R.drawable.ic_done);
                img_color4.setImageResource(0);
                img_color5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seclectedNoteColor = "#3A52FC";
                img_color1.setImageResource(0);
                img_color2.setImageResource(0);
                img_color3.setImageResource(0);
                img_color4.setImageResource(R.drawable.ic_done);
                img_color5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seclectedNoteColor = "#000000";
                img_color1.setImageResource(0);
                img_color2.setImageResource(0);
                img_color3.setImageResource(0);
                img_color4.setImageResource(0);
                img_color5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.view_color2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.view_color3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.view_color4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.view_color5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layout_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layout_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });

        if (alreadyAvailableNote != null){
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(bottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }

    private void showDeleteNoteDialog(){
        if (dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layout_delete_note_container)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    class DeleteNotetask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }

                    new DeleteNotetask().execute();

                }
            });

            view.findViewById(R.id.tv_cancel_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });

        }

        dialogDeleteNote.show();

    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) view_subtitle_indicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(seclectedNoteColor));
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    try {

                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        image_note.setImageBitmap(bitmap);
                        image_note.setVisibility(View.VISIBLE);
                        findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);

                        selectImagePath = getPathFromUri(selectedImageUri);

                    } catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri uri){
        String filePath;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null){
            filePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddUrlDialog(){
        if (dialogAddUrl == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layout_add_url_container)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText input_url = view.findViewById(R.id.input_url);
            input_url.requestFocus();

            view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (input_url.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNoteActivity.this, "Please Enter URL!", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(input_url.getText().toString().trim()).matches()){
                        Toast.makeText(CreateNoteActivity.this, "Please Valid URL!", Toast.LENGTH_SHORT).show();
                    } else {
                        tv_web_url.setText(input_url.getText().toString());
                        layout_web_url.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }
}