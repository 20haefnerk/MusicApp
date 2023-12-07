package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MusicPlayer extends AppCompatActivity {
    private final String TAG = "CPTR320";
    private final String PLAYER_POSITION_KEY = "CURR_POSITION";
    private final String PLAYER_STATE_KEY = "CURR_STATE";
    final MediaPlayer mediaPlayer = new MediaPlayer();
    MusicDatabase dbase = null;

    private String[] regPlaylist;
    private String[] shufPlaylist;
    private String[] currPlaylist;
    private boolean prepared = false;
    private boolean currState = false;
    private boolean shuffle_mode = false;
    private boolean looping_mode = false;

    private int count;

    private SeekBar seekBar;
    ImageButton playButton, pauseButton, stopButton, skipButton, backButton,
    forwardButton, backwardButton;

    private Timer timer;
    private TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        dbase = getIntent().getParcelableExtra(MusicApp.EXTRA_MESSAGE);
        shuffle_mode = getIntent().getBooleanExtra(MusicApp.EXTRA_SHUFFLE_MODE, false);
        looping_mode = getIntent().getBooleanExtra(MusicApp.EXTRA_LOOPING_MODE, false);

        regPlaylist = dbase.getTitles();
        shufPlaylist = Arrays.copyOf(regPlaylist, regPlaylist.length);
        knuthShuffle(shufPlaylist);

        if(shuffle_mode){
            currPlaylist = shufPlaylist;
        } else{
            currPlaylist = regPlaylist;
        }

        Song song = dbase.getSelection();
        setUpImage(song);

        seekBar = findViewById(R.id.seekBar);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        skipButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.previousButton);
        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.rewindButton);

        setUpSeekBar();
        setUpButtons();
    }

    private void setUpImage(Song song) {
        TextView textView = findViewById(R.id.textView);
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(getDrawable(song.getPicture()));
        imageView.setScaleX(song.getScaleX());
        imageView.setScaleY(song.getScaleY());
        textView.setText(dbase.getSelection().getTitle());
    }

    private void setUpButtons() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Media player play requested...");
                if(prepared) {
                    mediaPlayer.start();
                    currState = true;
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            int index = dbase.getCurrentSongIndex(dbase.getSelection().getTitle());

                            if(shuffle_mode ){
                                index = currentSelectionToShuffledIndex();
                                if (index == currPlaylist.length-1){
                                    knuthShuffle(currPlaylist);

                                }
                            }

                            if(!looping_mode){
                                if(!shuffle_mode){
                                    if(index ==currPlaylist.length-1){
                                        Toast toast = Toast.makeText(getApplicationContext(), "End of playlist", Toast.LENGTH_SHORT);
                                        toast.show();
                                        return;
                                    }
                                }
                            }

                            index = (index + 1) % currPlaylist.length;
                            dbase.setSelection(currPlaylist[index]);
                            mediaPlayer.reset();
                            setUpMediaPlayer();
                            setUpSeekBar();
                            setUpImage(dbase.getSelection());
                        }
                    });
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please wait!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        pauseButton.setOnClickListener(view -> {
            Log.d(TAG, "Pause requested...");
            if(prepared && mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                currState = false;
            }
        });

        skipButton.setOnClickListener(view -> {
            Log.d(TAG, "Skip requested...");
            int index = dbase.getCurrentSongIndex(dbase.getSelection().getTitle());
            if(shuffle_mode ){
                index = currentSelectionToShuffledIndex();
                if (index == currPlaylist.length-1){
                    knuthShuffle(currPlaylist);

                }
            }

            if(!looping_mode){
                if(!shuffle_mode){
                    if(index ==currPlaylist.length-1){
                        Toast toast = Toast.makeText(getApplicationContext(), "End of playlist", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                }
            }

                index = (index + 1) % currPlaylist.length;
                dbase.setSelection(currPlaylist[index]);
                mediaPlayer.reset();
                setUpMediaPlayer();
                setUpSeekBar();
                setUpImage(dbase.getSelection());
        });

        backButton.setOnClickListener(view -> {
            Log.d(TAG, "Previous requested...");
            int index = dbase.getCurrentSongIndex(dbase.getSelection().getTitle());
            String[] titles;
            if(shuffle_mode) {
                index = currentSelectionToShuffledIndex();
                if (index == currPlaylist.length - 1) {
                    knuthShuffle(currPlaylist);
                }
            }
            index = (index-1);
            if (index<0){
                index = currPlaylist.length-1;
            }
            dbase.setSelection(currPlaylist[index]);
            mediaPlayer.reset();
            setUpMediaPlayer();
            setUpSeekBar();
            setUpImage(dbase.getSelection());
        });

        backwardButton.setOnClickListener(view -> {
            Log.d(TAG, "rewind requested...");
            int time = 5000;
           int currentPosition = mediaPlayer.getCurrentPosition();
           if(currentPosition-time >0){
               currentPosition-=time;
               mediaPlayer.seekTo(currentPosition);
               seekBar.setProgress(currentPosition);
           }


        });

        forwardButton.setOnClickListener(view -> {
            Log.d(TAG, "fastforward requested...");
            int time = 5000;
            int currentPosition = mediaPlayer.getCurrentPosition();
            //if(currentPosition-time >0){
                currentPosition+=time;
                mediaPlayer.seekTo(currentPosition);
                seekBar.setProgress(currentPosition);
           // }


        });
    }

    private int currentSelectionToShuffledIndex() {
        String title = dbase.getSelection().getTitle();
        for (int i =0; i<currPlaylist.length; i++){
            if(currPlaylist[i].equalsIgnoreCase(title)){
                return i;
            }
        }
        return -1;
    }

    private void setUpSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "OnSeekbar listener...");
                int curr = seekBar.getProgress();
                mediaPlayer.seekTo(curr);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //currPosition = getPreferences(MODE_PRIVATE).getInt(PLAYER_POSITION_KEY, 0);
        currState = getPreferences(MODE_PRIVATE).getBoolean(PLAYER_STATE_KEY, false);

        //Log.d(TAG, "curr pos = " + currPosition + ", and curr state is " + currState);
        setUpMediaPlayer();
        setUpTimer();
    }

    private void setUpMediaPlayer() {
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            Log.d(TAG, "OnPrepared called...");
            prepared = true;
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setMin(0);
            mediaPlayer.seekTo(0);
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            if (currState){
                mediaPlayer.start();
            }

        });
        AssetFileDescriptor afd = getResources().openRawResourceFd(dbase.getSelection().getId());
        try {
            mediaPlayer.setDataSource(afd);
            mediaPlayer.prepareAsync();
            afd.close();
        } catch (IOException e){
            Log.d(TAG, "Exception when setting data source!");
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferences(MODE_PRIVATE).edit().putInt(PLAYER_POSITION_KEY,
                mediaPlayer.getCurrentPosition()).commit();
        getPreferences(MODE_PRIVATE).edit().putBoolean(PLAYER_STATE_KEY,
                mediaPlayer.isPlaying()).commit();
        prepared = false;
        mediaPlayer.reset();
        mediaPlayer.reset();
    }

    private void setUpTimer(){
        task = new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }
        };

        timer = new Timer();
        timer.schedule(task, 50, 200);
    }

    private String[] knuthShuffle(String [] keys){
        Random random = new Random();
        for (int i = 1; i< keys.length; i++){
            swap(i, random.nextInt(i+1), keys);
        }
        return keys;
    }

    private void swap(int i, int j, String []keys) {
        String tmp = keys[i];
        keys[i] = keys[j];
        keys[j] = tmp;
    }

}