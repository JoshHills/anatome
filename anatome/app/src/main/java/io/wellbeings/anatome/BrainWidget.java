package io.wellbeings.anatome;

/**
 * Created by Calum on 29/11/2015.
 * Purpose: To define the functionality of the brain section of the app
 */
import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BrainWidget extends Fragment implements Widget {

    // Store view object for UI manipulation.
    private View v;

    //declare variables for the graphical parts of the widget
    Button saveButton, galleryButton;

    //imageview to display a photo
    ImageView ivImage;


    ImageButton leftArrow;
    ImageButton rightArrow;

    //for audio capture
    private MediaPlayer mediaPlayer;
    private MediaRecorder recorder;
    private String OUTPUT_FILE;

    //for gallery
    private static final int RESULT_LOAD_IMG = 1;

    //list storing all the happynotes saved to file
    public static List<Note> noteList;
    //integer storing the current page of notes displayed
    //5 notes are displayed per page, indexing starts at 1
    private int noteListPage;

    //filename for persistent data
    private static final String FILE_NAME = "brain.txt";

    //required empty constructor
    public BrainWidget() { }
    //method for creating a new instance of this class
    public static BrainWidget newInstance() {
        BrainWidget fragment = new BrainWidget();
        Bundle args = new Bundle();
        return fragment;
    }
    //onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState); }

    //onCreateView method (when this fragment is navigated to)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, storing view.
        v = inflater.inflate(R.layout.fragment_brain_widget, container, false);

        //for the camera
        OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/audiorecorder.3gpp";






        //initialise list of notes from file
        noteList = getList();
        Log.d("noteListInit", noteList.toString());

        //initialise the graphics for the first five notes in the noteList
        for(int i = 4; i >= 0; i--) {
            if(i < noteList.size()) {
                initNote(noteList.get(i),0);
                initDeleteButton(noteList.get(i),0);
            }
        }

        //initialise the noteListPage to 1 (first index)
        noteListPage = 1;

        //add another note for the latest one
        initNote(new Note(getCurrentDate(), ""), 0);

        //initialise the control panel for saving and navigation
        initControlPanel(v);

        return v;
    }

    //method for initialising the save, left and right buttons
    private void initControlPanel(View v) {
        //initialise the buttons at the foot of the fragment
        saveButton = (Button) v.findViewById(R.id.btnSave1);
        leftArrow = (ImageButton) v.findViewById(R.id.leftArrow);
        rightArrow = (ImageButton) v.findViewById(R.id.rightArrow);
//        ivImage = (ImageView) v.findViewById(R.id.ivImage);
        galleryButton = (Button) v.findViewById(R.id.btnGallery);

        //obtain scroll view used in the save button's onclick listener
        final LinearLayout scroll = (LinearLayout) v.findViewById(R.id.noteScroll);

        //define the behaviour of saveButton on click
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Note note;
                try {
                    //retrieve the most recent note
                    note = getMostRecentNote();
                    //add the note to list
                    noteList.add(0,note);
                    //give the new note a delete button
                    initDeleteButton(note,0);
                    //ensure that no more than 5 notes are on display at once
                    if(scroll.getChildCount() > 5) {
                        Log.d("save", "too many children, will remove at last index");
                        //remove the oldest note from display
                        removedDisplayedSavedNote(5);
                    }
                }
                catch(NullPointerException e) {
                    Log.e("save", "no children in scroll");
                }


//                btnGallery.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    //open gallery
//                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                     //start the activity and pass the data
//                     startActivityForResult(i, RESULT_LOAD_IMG);
//                }
//                });

                        //update the list's state
                        saveList();
                List<Note> testList = getList();
                Toast.makeText(getContext(), "Gotlist: " + testList.toString(), Toast.LENGTH_LONG).show();


                //add another note to the end of the list
                initNote(new Note(getCurrentDate(), ""), 0);
            }
        });

         galleryButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
             //open gallery
             Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
             //start the activity and pass the data
             startActivityForResult(i, RESULT_LOAD_IMG);
             }
         });



        //define the behaviour of the left arrow
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateLeft();
            }
        });

        //define the behaviour of the right arrow
        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateRight();
            }
        });
    }

    //method for replacing the current notes on screen with the next 5 most recent
    private void navigateLeft() {
        //assert that it is not currently the first page
        if(noteListPage > 1) {
            //remove the notes on display from view
            removeDisplayedSavedNotes();

            //decrement the page number
            noteListPage--;

            //calculate the highest index note covered by the page number
            int maxIndex = (noteListPage * 5) - 1;
            int minIndex = maxIndex - 4;
            Log.d("noteListPage","maxIndex: " + maxIndex + " minIndex: " + minIndex);

            //add the five notes associated to the page number to view
            for(int i = maxIndex; i >= minIndex; i--) {
                initNote(noteList.get(i), 1);
                initDeleteButton(noteList.get(i), 1);
            }
        }
        else Log.d("noteListPage","noteListPage was <= 1");
    }

    //method for replacing the current notes on screen with the next 5 least recent
    private void navigateRight() {
        //assert that there are older notes to navigate to
        //multiplied by 5 for the number of notes per page
        if ((noteListPage * 5) < noteList.size()) {
            //remove the notes on display from view
            removeDisplayedSavedNotes();

            //increment the page number
            noteListPage++;

            //calculate the highest index note covered by the page number
            Log.d("noteListPage", "noteListPage = " + noteListPage);
            int maxIndex = (noteListPage * 5) - 1;
            int minIndex = maxIndex - 4;
            Log.d("noteListPage","maxIndex: " + maxIndex + " minIndex: " + minIndex);

            //add the five notes associated to the page number to view
            for(int i = maxIndex; i >= minIndex; i--) {
                Log.d("noteListPage", "i = " + i);
                if(i >= noteList.size()) {
                    Log.d("noteListPage", "i exceeded the noteList size");
                    continue; //skip if there is no note at this index
                }
                initNote(noteList.get(i), 1);
                initDeleteButton(noteList.get(i), 1);
            }
        } else Log.d("noteListPage", "noteListPage * 5 exceeded noteList size");
    }

    //method for removing a specific note from UI
    private void removedDisplayedSavedNote(int index) {
        try {
            LinearLayout scroll = (LinearLayout) v.findViewById(R.id.noteScroll);
            scroll.removeViewAt(index);
        }
        catch(Exception e) {
            //if something goes wrong, continue, it is not a critical operation
            Log.e("removeUI", "something went wrong");
            return;
        }
    }
    //method used in navigation for removing all displayed saved notes
    private void removeDisplayedSavedNotes() {
        try {
            LinearLayout scroll = (LinearLayout) v.findViewById(R.id.noteScroll);
            int maxIndex = scroll.getChildCount() - 1;
            //note: i must be greater than 0 to factor in the unsaved note
            for (int i = maxIndex; i > 0; i--) {
                scroll.removeViewAt(i);
            }
        }
        catch(Exception e) {
            //if something goes wrong, continue, it is not a critical operation
            Log.e("removeUI", "something went wrong");
            return;
        }
    }

    //method for obtaining most recent note
    private Note getMostRecentNote() throws NullPointerException {
        //obtain the scroll LinearLayout
        LinearLayout scroll = (LinearLayout) v.findViewById(R.id.noteScroll);

        //assert that there must be at least one note
        if(scroll.getChildCount() > 0) {
            //add most recent note to list
            LinearLayout endNote = (LinearLayout) scroll.getChildAt(0);
            TextView creationDate = (TextView) endNote.getChildAt(0);
            EditText content = (EditText) endNote.getChildAt(1);
            return new Note(creationDate.getText().toString(),
                    content.getText().toString());
        }
        else {
            throw new NullPointerException();
        }
    }

    //method for initialising notes
    //index is set to -1 for a note not part of the noteList
    private void initNote(final Note note, int index) {
        Log.d("noteListPage", "initNote called.");
        //obtain the horizontal scroll view that stores the notes
        final LinearLayout scroll = (LinearLayout)v.findViewById(R.id.noteScroll);


        //create a LinearLayout element
        final LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        //add the note's date textView
        TextView date = new TextView(getContext());
        date.setText(note.getCreationDate());
        ll.addView(date);

        //add the note's text input
        EditText noteInput = new EditText(getContext());
        noteInput.setLayoutParams(new ActionBar.LayoutParams(400, 400));
        noteInput.setText(note.getContent());
        ll.addView(noteInput);

        //change background colour of the linearLayout to white
        ll.setBackgroundColor(Color.parseColor("#FFFFFF"));

        //add Layout to the scrollview
        scroll.addView(ll, index);
    }

    //method for adding a delete button to a note
    private void initDeleteButton(final Note note, int index) {
        //obtain the note's linear layout
        final LinearLayout scroll = (LinearLayout)v.findViewById(R.id.noteScroll);
        final LinearLayout endNote = (LinearLayout)scroll.getChildAt(index);

        //when a note is deletable it should also be uneditable
        EditText noteInput = (EditText)endNote.getChildAt(1);
        noteInput.setEnabled(false);

        //add the delete button
        final ImageButton deleteButton = new ImageButton(getContext());
        //must use depreciated version because minimum API is set to 16.
        //we could include theme as a second param (not depreciated) but this requires API level 21
        Drawable d = getResources().getDrawable(R.drawable.bin);
        deleteButton.setImageDrawable(d);
        deleteButton.setBackground(null); //make background transparent
        endNote.addView(deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //set up resources for animation
                final ImageView smokeCloudImg = new ImageView(getContext());
                smokeCloudImg.setBackgroundResource(R.drawable.smoke);
                final AnimationDrawable smokeCloud =
                        (AnimationDrawable) smokeCloudImg.getBackground();

                //calculate the index endNote is at for animating
                int index = scroll.indexOfChild(endNote);

                //remove the UI for this note
                endNote.removeAllViews();
                scroll.removeView(endNote);

                // play the destruction animation
                // note: a margin has been given to position the animation more centrally to the note
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 150, 0, 0);
                smokeCloudImg.setLayoutParams(params);
                scroll.addView(smokeCloudImg, index);
                smokeCloud.start();

                //wait for animation to finish before removing it
                Handler handler = new Handler();
                //after the duration of the animation, send for its removal
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        smokeCloud.stop();
                        scroll.removeView(smokeCloudImg);
                    }
                }, 280);

                Log.d("REMOVAL", "Note: " + note + "?" + noteList.contains(note));

                //calculate the index in noteList the final note displayed is
                int maxIndex = (noteListPage * 5) - 1;

                noteList.remove(note);
                //load in a new note to take its place on the UI if it exists
                if (scroll.getChildCount() > 4) {
                    if (noteList.size() > maxIndex) {
                        Log.d("REMOVAL", "should be good to remove the maxIndex at index 5");
                        initNote(noteList.get(maxIndex), 5);
                        initDeleteButton(noteList.get(maxIndex), 5);
                    } else
                        Log.d("REMOVAL", "maxIndex exceeded final index, maxIndex: " + maxIndex + " noteList size: " + noteList.size());
                } else Log.d("REMOVAL", "getChildCount 4 or less");

                //save the updated list
                saveList();
                //play delete animation
            }
        });
    }

    //method for loading the notes into the fragment
    public List<Note> getList() {
        try{
            Log.d("GetList","getList called");
            return loadArrayList(FILE_NAME);
        }
       catch (IOException e){
            return new ArrayList<Note>();
        }
    }

    //method for updating the contents of the list in file
    public void saveList() {
        try {
            saveArrayList(getActivity().getApplicationContext(), FILE_NAME, noteList);
            Log.d("SaveList", "finished doing the save");
        } catch (IOException e) {
         e.printStackTrace();
        }
    }

    private void saveArrayList(Context context, String filename, List<Note> data) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();
        Toast.makeText(context, "The contents are saved in the file" + getList(), Toast.LENGTH_LONG).show();
    }

    private List<Note> loadArrayList(String filename) throws IOException {
        ArrayList<Note> readBack;

        try {
            FileInputStream fis = getActivity().openFileInput(filename);
            ObjectInputStream ois= new ObjectInputStream(fis);
            readBack = (ArrayList<Note>)ois.readObject();
            if(readBack==null){
                Log.e("null", "The noteList was read in as null.");
                readBack=new ArrayList<Note>();
            }
            ois.close();
            fis.close();
            return new ArrayList<Note>(readBack);
        }
        catch(ClassNotFoundException ex)
        {
            Log.e("loadlist","class not found",ex);
            return new ArrayList<Note>();
        }
        catch(IOException ex) {
            Log.e("loadlist", "io problem", ex);
            return new ArrayList<Note>();
        }
    }

    //method for retrieving the current date
    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.MONTH) + "/" + c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.YEAR);
        return date;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_LOAD_IMG) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = {MediaStore.Images.Media.DATA};

         ContentResolver contentResolverU = ContentResolverUltility.tryGetContentResolver(getContext());
           Cursor cursor = contentResolverU.query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            // Now we need to set the GUI ImageView data with data read from the picked file.
//            ivImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
//            Toast.makeText(this, "Image picked from gallery", Toast.LENGTH_LONG).show();
        }

    }




//
//    public void onStartRecording(View v){
//        try{
//            beginRecording();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
//    public void onStopRecording(View v){
//
//        try{
//            stopRecording();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    public void onPlay(View v){
//        try{
//            playRecording();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//
//    public void onPause(View v){
//        try{
//            stopPlayback();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//
//
//    public void beginRecording() throws IOException {
//        ditchMediaRecorder();
//        File outFile = new File(OUTPUT_FILE);
//        if (outFile.exists())
//            outFile.delete();
//
//        recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        recorder.setOutputFile(OUTPUT_FILE);
//        try{
//            recorder.prepare();
//        }catch (Exception e){
//            e.printStackTrace();
//
//        }
//
//        recorder.start();
//
//
//    }
//
//    private void ditchMediaRecorder() {
//        if(recorder != null)
//            recorder.release();
//    }
//
//    public void stopRecording(){
//        if(recorder != null)
//            recorder.stop();
//
//    }
//
//    public void playRecording() throws IOException {
//        ditchMediaPlayer();
//        mediaPlayer=new MediaPlayer();
//        mediaPlayer.setDataSource(OUTPUT_FILE);
//        mediaPlayer.prepare();
//        mediaPlayer.start();
//
//    }
//
//    private void ditchMediaPlayer() {
//        if(mediaPlayer != null){
//            try{
//                mediaPlayer.release();
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void stopPlayback(){
//        if(mediaPlayer != null)
//            mediaPlayer.stop();
//
//    }





}