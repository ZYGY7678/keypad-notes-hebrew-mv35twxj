package com.generated.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteEditActivity extends Activity {

    private NoteDbHelper dbHelper;
    private long noteId = -1;
    private EditText etTitle;
    private EditText etContent;
    private Button btnSave;
    private Button btnDelete;
    private TextView tvHeaderTitle;
    private TextView tvEditDate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        dbHelper = new NoteDbHelper(this);

        etTitle = (EditText) findViewById(R.id.et_note_title);
        etContent = (EditText) findViewById(R.id.et_note_content);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        tvHeaderTitle = (TextView) findViewById(R.id.tv_header_title);
        tvEditDate = (TextView) findViewById(R.id.tv_edit_date);

        noteId = getIntent().getLongExtra("note_id", -1);

        if (noteId != -1) {
            tvHeaderTitle.setText(R.string.edit_note_title);
            Note note = dbHelper.getNote(noteId);
            if (note != null) {
                etTitle.setText(note.getTitle());
                etContent.setText(note.getContent());
                tvEditDate.setText(dateFormat.format(new Date(note.getTimestamp())));
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            tvHeaderTitle.setText(R.string.new_note_title);
            tvEditDate.setText(dateFormat.format(new Date(System.currentTimeMillis())));
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        etTitle.requestFocus();
    }

    private void saveNote() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, R.string.empty_note_not_saved, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        long now = System.currentTimeMillis();
        if (noteId == -1) {
            Note newNote = new Note(title, content, now);
            dbHelper.insertNote(newNote);
        } else {
            Note updateNote = new Note(noteId, title, content, now);
            dbHelper.updateNote(updateNote);
        }

        Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (noteId != -1) {
                            dbHelper.deleteNote(noteId);
                            Toast.makeText(NoteEditActivity.this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
