package com.generated.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_EDIT = 101;

    private NoteDbHelper dbHelper;
    private EditText etSearch;
    private Button btnNewNote;
    private ListView listNotes;
    private View layoutEmptyState;
    private TextView tvNoteCount;

    private NoteAdapter adapter;
    private final List<Note> noteList = new ArrayList<Note>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NoteDbHelper(this);

        etSearch = (EditText) findViewById(R.id.et_search);
        btnNewNote = (Button) findViewById(R.id.btn_new_note);
        listNotes = (ListView) findViewById(R.id.list_notes);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvNoteCount = (TextView) findViewById(R.id.tv_note_count);

        adapter = new NoteAdapter();
        listNotes.setAdapter(adapter);

        // Keypad selection mode: keep selected items visible
        listNotes.setItemsCanFocus(true);

        btnNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNoteEditor(-1);
            }
        });

        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = noteList.get(position);
                openNoteEditor(note.getId());
            }
        });

        listNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showNoteOptionsDialog(noteList.get(position));
                return true;
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadNotes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadNotes("");
        // Ensure default focus on the New Note button for keypad users
        btnNewNote.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentQuery = etSearch.getText() != null ? etSearch.getText().toString() : "";
        loadNotes(currentQuery);
    }

    private void loadNotes(String query) {
        noteList.clear();
        noteList.addAll(dbHelper.searchNotes(query));
        adapter.notifyDataSetChanged();

        int count = noteList.size();
        tvNoteCount.setText(getString(R.string.count_notes_format, count));

        if (count == 0) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            listNotes.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            listNotes.setVisibility(View.VISIBLE);
        }
    }

    private void openNoteEditor(long noteId) {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra("note_id", noteId);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

    private void showNoteOptionsDialog(final Note note) {
        CharSequence[] options = new CharSequence[]{
                getString(R.string.edit),
                getString(R.string.delete)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(note.getTitle().length() > 0 ? note.getTitle() : getString(R.string.options_dialog_title));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openNoteEditor(note.getId());
                } else if (which == 1) {
                    confirmDelete(note);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void confirmDelete(final Note note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteNote(note.getId());
                        Toast.makeText(MainActivity.this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        loadNotes(etSearch.getText() != null ? etSearch.getText().toString() : "");
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Physical key 1 -> New Note shortcut
        if (keyCode == KeyEvent.KEYCODE_1) {
            if (!etSearch.hasFocus()) {
                openNoteEditor(-1);
                return true;
            }
        }
        // Physical key 2 -> Delete currently selected item shortcut
        if (keyCode == KeyEvent.KEYCODE_2) {
            if (!etSearch.hasFocus()) {
                int pos = listNotes.getSelectedItemPosition();
                if (pos >= 0 && pos < noteList.size()) {
                    confirmDelete(noteList.get(pos));
                    return true;
                }
            }
        }
        // Physical Menu key -> Options on selected note
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            int pos = listNotes.getSelectedItemPosition();
            if (pos >= 0 && pos < noteList.size()) {
                showNoteOptionsDialog(noteList.get(pos));
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class NoteAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return noteList.size();
        }

        @Override
        public Object getItem(int position) {
            return noteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return noteList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.item_note, parent, false);
                holder = new ViewHolder();
                holder.tvIndex = (TextView) convertView.findViewById(R.id.item_tv_index);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.item_tv_title);
                holder.tvDate = (TextView) convertView.findViewById(R.id.item_tv_date);
                holder.tvSnippet = (TextView) convertView.findViewById(R.id.item_tv_snippet);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Note note = noteList.get(position);
            holder.tvIndex.setText(String.valueOf(position + 1));

            String title = note.getTitle().trim();
            if (title.isEmpty()) {
                title = "(ללא כותרת)";
            }
            holder.tvTitle.setText(title);

            String content = note.getContent().trim();
            if (content.isEmpty()) {
                holder.tvSnippet.setText("(ללא תוכן)");
            } else {
                holder.tvSnippet.setText(content);
            }

            holder.tvDate.setText(dateFormat.format(new Date(note.getTimestamp())));

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tvIndex;
        TextView tvTitle;
        TextView tvDate;
        TextView tvSnippet;
    }
}
