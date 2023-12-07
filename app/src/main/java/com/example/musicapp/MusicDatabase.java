package com.example.musicapp;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicDatabase implements Parcelable {

    private String selection;
    private HashMap<String, Song> database = new HashMap<>();
    private List<String> catList = new ArrayList<>();


    public MusicDatabase(String title){
        this.selection = title;
    }


    public MusicDatabase(Context context){
        Song sudokuSong = new Song(R.raw.game, context.getString(R.string.title1), R.drawable.rainbow_sudoku);
        Song sudokuSong2 = new Song(R.raw.main, context.getString(R.string.title2), R.drawable.rainbow_sudoku);
        Song ukuleleSong = new Song(R.raw.ukulele, context.getString(R.string.title3), R.drawable.ukulele);
        Song pianoSong = new Song(R.raw.piano, context.getString(R.string.title4), R.drawable.piano);
        Song trumpetSong = new Song(R.raw.trumpet,context.getString(R.string.title5), R.drawable.trumpet);

        database.put(sudokuSong.getTitle(), sudokuSong);
        database.put(sudokuSong2.getTitle(), sudokuSong2);
        database.put(ukuleleSong.getTitle(), ukuleleSong);
        database.put(pianoSong.getTitle(), pianoSong);
        database.put(trumpetSong.getTitle(), trumpetSong);

        catList.add(sudokuSong.getTitle());
        catList.add(sudokuSong2.getTitle());
        catList.add(ukuleleSong.getTitle());
        catList.add(pianoSong.getTitle());
        catList.add(trumpetSong.getTitle());

    }
    protected MusicDatabase(Parcel in) {
        selection = in.readString();
        Object obj = in.readHashMap(getClass().getClassLoader());
        if(obj instanceof HashMap){
            database = (HashMap<String, Song>) obj;
        }
        catList = in.readArrayList(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selection);
        dest.writeMap(database);
        dest.writeList(catList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MusicDatabase> CREATOR = new Creator<MusicDatabase>() {
        @Override
        public MusicDatabase createFromParcel(Parcel in) {
            return new MusicDatabase(in);
        }

        @Override
        public MusicDatabase[] newArray(int size) {
            return new MusicDatabase[size];
        }
    };
    public Song getSelection(){
        return database.get(selection);
    }
    public String[] getTitles(){
        return catList.toArray(new String[0]);
    }
    public int getCurrentSongIndex(String title){
        String[] titles = getTitles();
        for (int index =0; index < titles.length; index++){
            if (title.equalsIgnoreCase(titles[index]))
                return index;
        }
        return -1;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public HashMap<String, Song> getDatabase() {
        return database;
    }

    public void setDatabase(HashMap<String, Song> database) {
        this.database = database;
    }

}
