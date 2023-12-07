package com.example.musicapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MusicApp extends AppCompatActivity {

    private final String TAG = "CPTR320";
    public final static String EXTRA_MESSAGE = "STRING_EXTRA";
    public final static String EXTRA_SHUFFLE_MODE = "SHUFFLE_MODE";
    public final static String EXTRA_LOOPING_MODE = "LOOPING_MODE";


    private View currentSelection = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MusicDatabase dbase = new MusicDatabase(this);
        ListView listView = findViewById(R.id.play_list);
        String[] array = dbase.getTitles();
        PlayList playList = new PlayList(this, android.R.layout.simple_list_item_1, array);
        listView.setAdapter(playList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        String content = (String) parent.getItemAtPosition(position);
                        currentSelection = view;
                        dbase.setSelection(content);
                        Log.d(TAG, "Index clicked is " + position + " set to " + content);
                        Intent intent = new Intent(getApplicationContext(), MusicPlayer.class);
                        intent.putExtra(EXTRA_MESSAGE, dbase);
                        intent.putExtra(EXTRA_SHUFFLE_MODE, PreferenceActivity.getShuffle(MusicApp.this));
                        intent.putExtra(EXTRA_LOOPING_MODE, PreferenceActivity.getLoop(MusicApp.this));
                        startActivity(intent);
                    }
                });

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if(id == R.id.settings){
            Log.d(TAG, "settings clicked");
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        }
        if(id == R.id.about){
            Log.d(TAG, "About clicked");
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentSelection != null){
            currentSelection.setAlpha(1.0f);
        }
    }
}