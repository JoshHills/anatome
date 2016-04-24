package io.wellbeings.anatome;

/**
 * Interactive subsection hinging on body part
 * provides a CBT technique of saveing positive
 * thought objects and expelling negative ones.
 *
 * @author Team WellBeings - Calum, Thirawat
 */
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.session.MediaController;
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
import android.content.Context;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BrainWidget extends Fragment implements Widget {

    // Store name of section.
    private final String SECTION = "brain";
    // Store view object for UI manipulation.
    private View v;

    //declare variables for the graphical parts of the widget
    private ImageButton saveButton, galleryButton, leftArrow, rightArrow,
         negativeDeleteButton, audioButton;
    private EditText newNoteContent;

    //MediaPlayer used to handle note playback
    MediaPlayer mp;
    AudioManager audioManager;

    //data used for audio support
    private int currentSongIndex = 0;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    //result code constants for image and audio selection
    private static final int RESULT_LOAD_IMG = 1;
    private static final int RESULT_LOAD_AUDIO = 100;

    //filename for persistent data
    private static final String FILE_NAME = "brain.txt";

    //list storing all the happynotes saved to file
    private static List<Note> noteList;
    //integer storing the current page of notes displayed
    private int noteListPage; //indexing starts at 1
    //constant storing the maximum number of notes per page
    private static int MAX_NOTES_PER_PAGE = 5;
    //integer storing the current day streak the user has saving notes
    private int dayStreak;

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

        //initialise tools for handling audio in the app
        mp = new MediaPlayer();
        audioManager = new AudioManager();

        //initialise the noteListPage to 1 (first index)
        noteListPage = 1;

        //initialise the main GUI elements
        initGUI();

        //calculate the initial streak of days saving notes and display it
        displayDayStreak();

        //attach listeners for the main UI component
        attachListeners();

        return v;
    }

    public void initGUI() {

        // Set textual content.

        ((TextView) v.findViewById(R.id.brain_instruction_positive)).setText(
                UtilityManager.getContentLoader(getContext()).getInfoText(SECTION, "instruction-positive")
        );

        ((TextView) v.findViewById(R.id.brain_instruction_negative)).setText(
                UtilityManager.getContentLoader(getContext()).getInfoText(SECTION, "instruction-negative")
        );

        //initialise list of notes from file
        noteList = getList();

        //if there aren't any notes then display the tutorial note
        if(noteList.size() == 0) {
            Note note = new Note(getCurrentDate(),
                    getResources().getString(R.string.tutorial_note));
            initNote(note,0);
        }
        else {
            //initialise the graphics for the first five notes in the noteList
            for (int i = 4; i >= 0; i--) {
                if (i < noteList.size()) {
                    initNote(noteList.get(i), 0);
                }
            }
        }
    }

    //method contains attachment of listeners for the main UI component (not including saved notes)
    public void attachListeners() {
        //retrieve the two navigation buttons
        leftArrow = (ImageButton) v.findViewById(R.id.brain_left_arrow);
        rightArrow = (ImageButton) v.findViewById(R.id.brain_right_arrow);

        //obtain scroll view used in the save button's onclick listener
        final LinearLayout scroll = (LinearLayout) v.findViewById(R.id.brain_notes_scroll);

        //retrieve the elements of the new note
        newNoteContent = (EditText) v.findViewById(R.id.brain_new_note_content);
        audioButton = (ImageButton) v.findViewById(R.id.brain_audio_button);
        saveButton = (ImageButton) v.findViewById(R.id.brain_save_button);
        galleryButton = (ImageButton) v.findViewById(R.id.brain_gallery_button);
        //retreive the negative note's delete button
        negativeDeleteButton = (ImageButton) v.findViewById(R.id.brain_negative_delete);

        //define the behaviour of saveButton on click
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //create a note object out of the new notes details
                String date = getCurrentDate();
                String content = newNoteContent.getText().toString();
                Note note = new Note(date, content);

                //save the note
                saveNote(note);
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

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Getting all audios
                songsList = audioManager.getPlayList();
                Intent i = new Intent(getActivity().getApplicationContext(), PlayListActivity.class);
                //Intent intent_upload = new Intent();
                //intent_upload.setType("audio/*");
                //intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                //startActivityForResult(intent_upload,1);
                startActivityForResult(i, RESULT_LOAD_AUDIO);
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

        //define the behaviour of the negative note's delete button
        negativeDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //play the delete animation to simulate destroying the note
                /*
                    NOTE: a negative value indicates to the function that the note
                    being deleted is the negative note at the bottom of the layout
                 */
                playDeleteAnimation(-1);
            }
        });
    }

    /*method for calculating the number of days a note has been set for,
      and then displaying this in the appropriate TextView*/
    private void displayDayStreak(){
        //start by initialising dayStreak to 0
        dayStreak = 0;

        try {
            //define the format that the current date will be retrieved in
            SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");

            //fetch the current date in a format simple to compare
            Date d = sdf.parse(getCurrentDate());

            //calculate how long the day streak actually is
            while(dayStreak < noteList.size()) {
                //if the date of this note is the same as the previous, skip it
                //first check that is not there is a previous value
                if(dayStreak > 0 && (noteList.get(dayStreak).getCreationDate())
                        == (noteList.get(dayStreak-1).getCreationDate())) {
                    continue;
                }

                //parse the date of the note in question to a date object
                Date noteDate = sdf.parse(noteList.get(dayStreak).getCreationDate());

                //calculate how many days ago the next most recent note was
                long streak = d.getTime() - noteDate.getTime();

                //if it's been more than 24 hours, break as the streak has ended
                if (streak > 8.64e+7) break;

                    //otherwise, increment streak by 1, and repeat with the following note
                else dayStreak++;
            }
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        //display the day streak to the user
        TextView streak = (TextView)v.findViewById(R.id.brain_streak);
        streak.setText("streak of storing happy notes: " + dayStreak + " days!");
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
            int maxIndex = (noteListPage * MAX_NOTES_PER_PAGE) - 1;
            int minIndex = maxIndex - 4;

            //add the five notes associated to the page number to view
            for(int i = maxIndex; i >= minIndex; i--) {
                initNote(noteList.get(i), 0);
            }
        }
    }

    //method for replacing the current notes on screen with the next 5 least recent
    private void navigateRight() {
        //assert that there are older notes to navigate to
        //multiplied by 5 for the number of notes per page
        if ((noteListPage * MAX_NOTES_PER_PAGE) < noteList.size()) {
            //remove the notes on display from view
            removeDisplayedSavedNotes();

            //increment the page number
            noteListPage++;

            //calculate the highest index note covered by the page number
            int maxIndex = (noteListPage * MAX_NOTES_PER_PAGE) - 1;
            int minIndex = maxIndex - 4;

            //add the five notes associated to the page number to view
            for (int i = maxIndex; i >= minIndex; i--) {
                if (i >= noteList.size()) {
                    continue; //skip if there is no note at this index
                }
                initNote(noteList.get(i), 0);
            }
        }
    }

    //method for removing a specific note from UI
    private void removedDisplayedSavedNote(int index) {
        try {
            LinearLayout scroll = (LinearLayout) v.findViewById(R.id.brain_notes_scroll);
            scroll.removeViewAt(index);
        }
        catch(Exception e) {
            //if something goes wrong, continue, it is not a critical operation
            return;
        }
    }
    //method used in navigation for removing all displayed saved notes
    private void removeDisplayedSavedNotes() {
        try {
            LinearLayout scroll = (LinearLayout) v.findViewById(R.id.brain_notes_scroll);
            int maxIndex = scroll.getChildCount() - 1;
            for (int i = maxIndex; i >= 0; i--) {
                scroll.removeViewAt(i);
            }
        }
        catch(Exception e) {
            //if something goes wrong, continue, it is not a critical operation
            return;
        }
    }

    /*
        methods for initialising notes
     */
    //method for displaying a saved note
    private void initNote(final Note note, int index) {
        //obtain the horizontal scroll view that stores the notes
        final LinearLayout scroll = (LinearLayout)v.findViewById(R.id.brain_notes_scroll);

        //obtain and inflate the note's appearance from an XML definition
        LayoutInflater inflater;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.note_layout, scroll, false);

        //set the date of the note being initialised
        ((TextView)ll.findViewById(R.id.brain_note_date)).setText(note.getCreationDate());

        //if the note has image content, load this content in
        if(note.hasImageContent()) {
            ImageView iv = (ImageView)ll.findViewById(R.id.brain_note_image_content);

            //retrieve the image content and add it to view
            String imageDir = note.getImageDirectory();
            iv.setImageBitmap(BitmapFactory.decodeFile(imageDir));
            iv.setVisibility(View.VISIBLE); //make the image content visible
        }

        //if the note has audio content, prepare the playback for this content
        else if(note.hasAudioContent()){
            final EditText status = (EditText) ll.findViewById(R.id.brain_note_audio_status);
            SeekBar seekBar = (SeekBar)ll.findViewById(R.id.brain_note_seek_bar);

            ImageButton btnPlay = (ImageButton) ll.findViewById(R.id.brain_note_play);
            ImageButton stopButton = (ImageButton) ll.findViewById(R.id.brain_note_stop);

            ll.findViewById(R.id.brain_note_audio_content).setVisibility(View.VISIBLE);

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio(status);
                }
            });
            btnPlay.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    playAudio(status, note);
                }
            });
        }

        //else, the content is text based
        else {
            //add the note's text input
            EditText noteInput = (EditText)ll.findViewById(R.id.brain_note_text_content);
            noteInput.setText(note.getContent());
            noteInput.setVisibility(View.VISIBLE); //make the content visible
        }

        //add the delete button
        final ImageButton deleteButton = (ImageButton)ll.findViewById(R.id.brain_note_delete);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //calculate the index endNote is at for animating
                int index = scroll.indexOfChild(ll);

                //remove the UI for this note
                ll.removeAllViews();
                scroll.removeView(ll);

                // play the destruction animation
                playDeleteAnimation(index);

                //calculate the index in noteList the final note displayed is
                int maxIndex = (noteListPage * MAX_NOTES_PER_PAGE) - 1;

                noteList.remove(note);
                //load in a new note to take its place on the UI if it exists
                if (scroll.getChildCount() > 4) {
                    if (noteList.size() > maxIndex) {
                        initNote(noteList.get(maxIndex), MAX_NOTES_PER_PAGE);
                    }
                }

                //save the updated list
                saveList();
            }
        });

        //add Layout to the scrollview
        scroll.addView(ll, index);
    }

    //method for playing the smoke cloud animation associated to deletion
    private void playDeleteAnimation(int index) {
        //set up resources for animation
        final ImageView smokeCloudImg = new ImageView(getContext());
        smokeCloudImg.setBackgroundResource(R.drawable.smoke);
        final AnimationDrawable smokeCloud =
                (AnimationDrawable) smokeCloudImg.getBackground();
        //create LayoutParamaters for the animation
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //linearLayout will be the container of the note being deleted
        final LinearLayout container;
        //obtain the negative thought EditText
        EditText negativeText = (EditText)v.findViewById(R.id.brain_negative_text);

        //a negative index indicates the note being deleted is the "negative thought note"
        if(index < 0){
            //obtain the linear layout holding the negative note
            container = (LinearLayout)v.findViewById(R.id.brain_negative_note);
            //temporarily remove the negative thought EditText
            negativeText.setText("");
            container.removeView(negativeText);
            //set margin placing the animation more centrally for the note
            params.setMargins(300, 0, 0, 0);
            smokeCloudImg.setLayoutParams(params);
            //add the animation in its place
            container.addView(smokeCloudImg, 0);
        }
        //a positive index indicates the note being deleted is saved in the scroll
        else {
            //obtain the horizontal scroll view that stores the notes
            container = (LinearLayout)v.findViewById(R.id.brain_notes_scroll);
            //set margin placing the animation more centrally for a note
            params.setMargins(0, 150, 0, 0);
            smokeCloudImg.setLayoutParams(params);
            //add the animation to the scrollview
            container.addView(smokeCloudImg, index);
        }
        smokeCloud.start();
        //wait for animation to finish before removing it
        Handler handler = new Handler();
        //after the duration of the animation, send for its removal
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                smokeCloud.stop();
                container.removeView(smokeCloudImg);
            }
        }, 280);

        //if necessary, re-add the negative thoughts view
        if(index < 0) container.addView(negativeText,0);
    }

    //method for loading the notes into the fragment
    private List<Note> getList() {
        try{
            return loadArrayList(FILE_NAME);
        }
       catch (IOException e){
            return new ArrayList<Note>();
        }
    }

    //method for saving a new note
    private void saveNote(Note note) {
        //obtain the scroll view the note will be displayed in
        final LinearLayout scroll = (LinearLayout)v.findViewById(R.id.brain_notes_scroll);

        //add the note to list
        noteList.add(0, note);

        //initialise the graphics of the note
        initNote(note,0);
        //ensure that no more than 5 notes are on display at once
        if(scroll.getChildCount() > MAX_NOTES_PER_PAGE) {
            //remove the oldest note from display
            removedDisplayedSavedNote(MAX_NOTES_PER_PAGE);
        }

        //update the list's state
        saveList();

        //reset the content of the new note
        newNoteContent.setText("");
    }

    //method for updating the contents of the list in file
    private void saveList() {
        try {
            saveArrayList(getActivity().getApplicationContext(), FILE_NAME, noteList);
            //calculate and update the number of consecutive days posting a note
            displayDayStreak();
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
    }

    private List<Note> loadArrayList(String filename) throws IOException {
        ArrayList<Note> readBack;

        try {
            FileInputStream fis = getActivity().openFileInput(filename);
            ObjectInputStream ois= new ObjectInputStream(fis);
            readBack = (ArrayList<Note>)ois.readObject();
            if(readBack==null){
                readBack=new ArrayList<Note>();
            }
            ois.close();
            fis.close();
            return new ArrayList<Note>(readBack);
        }
        catch(ClassNotFoundException ex)
        {
            return new ArrayList<Note>();
        }
        catch(IOException ex) {
            return new ArrayList<Note>();
        }
    }

    //method for retrieving the current date
    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        String date =  c.get(Calendar.DAY_OF_MONTH) + "/"
                + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR);
        return date;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG) {
            //check the data is not null
            if (data != null) {
                Uri pickedImage = data.getData();
                // Let's read picked image path using content resolver
                String[] filePath = {MediaStore.Images.Media.DATA};


                Cursor cursor = getContext().getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                //create a note object with the image directory
                String date = getCurrentDate();
                Note note = new Note(date, "");
                note.setImageContent(imagePath);

                //save note
                saveNote(note);
                //close cursor
                cursor.close();
            }
        }
        if(resultCode == RESULT_LOAD_AUDIO){
            //check the data is not null
            if(data != null) {
                currentSongIndex = data.getExtras().getInt("songIndex");
                String audioPath = songsList.get(currentSongIndex).get("songPath");

                Log.d("audio","song path = " + audioPath);

                //create and save the note object for it
                String date = getCurrentDate();
                Note note = new Note(date, "");
                note.setAudioContent(audioPath);
                saveNote(note);
            }
        }
    }

    //play the audio from a note object
    public void  playAudio(EditText status, Note note){
        // play audio
        try {
            mp.reset();
            mp.setDataSource(note.getAudioDirectory());
            mp.prepare();
            status.setText(getResources().getString(R.string.playback_status_playing));
            mp.start();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void stopAudio(EditText status){
        if(mp.isPlaying()){
            status.setText(getResources().getString(R.string.playback_status_stopped));
            mp.stop();
        }
    }
}