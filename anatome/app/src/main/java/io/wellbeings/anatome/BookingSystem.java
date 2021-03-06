package io.wellbeings.anatome;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Interactive subsection hinging on body part
 * provides a unit calculator to monitor
 * alcohol consumption of user.
 *
 * @author Team WellBeings - Callum, Bettina, Josh
 */
public class BookingSystem extends AppCompatActivity implements Widget {

    // Store whether the user has interacted with form elements.
    private boolean hasInteracted = false;
    DatePickerDialog dp;

    // Retrieve display elements for code clarity.
    private TextView mSetDate, mSetTime, mBookingTitle;
    private Button mBackFromBooking, mBook;
    private ImageButton mOptions;

    // Store private fields used to construct an appointment.
    private String[] appointments;
    private String pref = "either";
    private String timeTime = UtilityManager.getContentLoader(this).getButtonText("time");
    private final Calendar c = Calendar.getInstance();

    // Create private date listener.
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            // Set the date within the calendar object.
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setCurrentDateOnView();
        }
    };

    /* Necessary lifecycle methods. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_layout);

        initGUI();

        //initialise date
        String dateFormat = "dd-MM-yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.UK);
        c.add(Calendar.DATE, 7);
        mSetDate.setText(sdf.format(c.getTime()));

        createDatePicker();

        attachListeners();
    }

    public void initGUI() {

        // Find and save references to commonly used UI elements.
        mSetDate = (TextView) findViewById(R.id.booking_set_date);
        mSetTime = (TextView) findViewById(R.id.booking_set_time);
        mBackFromBooking = (Button) findViewById(R.id.booking_back_from_booking);
        mBook = (Button) findViewById(R.id.booking_book_from_booking);
        mBookingTitle = (TextView) findViewById(R.id.booking_title);

        // Set textual content.

        mBackFromBooking.setText(
                UtilityManager.getContentLoader(this).getButtonText("back")
        );
        mBook.setText(
                UtilityManager.getContentLoader(this).getButtonText("book")
        );
        mBookingTitle.setText(
                UtilityManager.getContentLoader(this).getButtonText("appointment")
        );

        // Set fonts.
        Typeface customFont = UtilityManager.getThemeUtility(this).getFont("Bariol");

        mSetDate.setTypeface(customFont);
        mSetTime.setTypeface(customFont);
        mBackFromBooking.setTypeface(customFont);
        mBook.setTypeface(customFont);
        mBookingTitle.setTypeface(customFont);

        // Set the current date.
        setCurrentDateOnView();

        // Disable the booking button if nothing has been selected yet.
        if(!hasInteracted) {
            disableBookButton();
        }
    }

    public void attachListeners() {

        // Add back button.
        mBackFromBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to main scroll.
                finish();
            }
        });

        // Add submit button.
        mBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postBooking();

                // Switch to new view.
                // TODO: CLEAR ACTIVITY STACK.
                Intent intent = new Intent(BookingSystem.this, AppointmentDetails.class);
                startActivity(intent);

                // Remove this one from the stack.
                finish();
            }
        });

        // Add filter screen navigation button.
        mOptions = (ImageButton) findViewById(R.id.booking_options);
        mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayOptions();
            }
        });
    }

    /* Useful extraneous methods. */

    /**
     * Format the selected date and time,
     * display from held calendar object.
     */
    public void setCurrentDateOnView() {

        // Set the correct format wanted.
        String dateFormat = "dd-MM-yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.UK);
        // Display the new date and time.
        mSetDate.setText(sdf.format(c.getTime()));
        mSetTime.setText(timeTime);
    }

    /**
     * Display the referenced sub-view with
     * styling and functionality.
     */
    private void displayOptions() {

        // Change the view.
        setContentView(R.layout.booking_options_layout);

        // Style the view.

        Button save = (Button) findViewById(R.id.booking_save_gender);
        TextView firstLine = (TextView) findViewById(R.id.booking_gender_line_one);
        TextView secondLine = (TextView) findViewById(R.id.booking_gender_line_two);
        TextView smallText = (TextView) findViewById(R.id.booking_gender_info);
        RadioButton rb1 = (RadioButton) findViewById(R.id.booking_radio_woman);
        RadioButton rb2 = (RadioButton) findViewById(R.id.booking_radio_man);
        RadioButton rb3 = (RadioButton) findViewById(R.id.booking_radio_nopreference);

        // Set the content.

        save.setText(
                UtilityManager.getContentLoader(this).getButtonText("save")
        );
        firstLine.setText(
                UtilityManager.getContentLoader(this).getButtonText("filter-one")
        );
        secondLine.setText(
                UtilityManager.getContentLoader(this).getButtonText("filter-two")
        );
        smallText.setText(
                UtilityManager.getContentLoader(this).getButtonText("filter-info")
        );
        rb1.setText(
                UtilityManager.getContentLoader(this).getButtonText("woman")
        );
        rb2.setText(
                UtilityManager.getContentLoader(this).getButtonText("man")
        );
        rb3.setText(
                UtilityManager.getContentLoader(this).getButtonText("no-preference")
        );

        Typeface bariol = UtilityManager.getThemeUtility(this).getFont("Bariol");
        Typeface helvetica = UtilityManager.getThemeUtility(this).getFont("Helvetica");

        save.setTypeface(helvetica);
        firstLine.setTypeface(bariol);
        secondLine.setTypeface(bariol);
        smallText.setTypeface(helvetica);
        rb1.setTypeface(helvetica);c.add(Calendar.DATE, 1);
        rb2.setTypeface(helvetica);
        rb3.setTypeface(helvetica);

        // Provide a listener to revert the view.
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.booking_layout);
                initGUI();
                attachListeners();
            }
        });

    }

    /**
     * Styling function for brevity disables button.
     */
    private void disableBookButton() {
        mBook.setEnabled(false);
        mBook.setTextColor(Color.parseColor("#BBBBBB"));
    }

    /**
     * Styling function for brevity enables button,
     * also logs interaction.
     */
    private void enableBookButton() {
        hasInteracted = true;
        mBook.setEnabled(true);
        mBook.setTextColor(Color.parseColor("#09849a"));
    }

    /* Methods fired by XML elements. */

    /**
     * Provide method of accessing available appointment
     * times, populating content of subsequent dialog
     * with their information - accessed by XML element.
     *
     * @param view  The initiating button.
     */
    public void timeOnClick(View view) {

        // Get the current date.
        String date = mSetDate.getText().toString();
        String newDate = null;

        // Reformat it.
        SimpleDateFormat initial = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat needed = new SimpleDateFormat("yyyy-MM-dd");

        try {

            newDate = needed.format(initial.parse(date));

            //Number Picker adapted from http://stackoverflow.com/questions/17805040/how-to-create-a-number-picker-dialog

            // Retrieve a list of appointments, sort them.
            appointments = UtilityManager.getDbUtility(this).getAvailable(newDate.toString());
            Arrays.sort(appointments);

            // create a number picker for use with strings
            RelativeLayout linearLayout = new RelativeLayout(BookingSystem.this);
            final NumberPicker myNumberPicker = new NumberPicker(BookingSystem.this);
            myNumberPicker.setMaxValue(appointments.length - 1);

            //min value
            myNumberPicker.setMinValue(0);

            //set values to strings from DB
            myNumberPicker.setDisplayedValues(appointments);

            myNumberPicker.setOnValueChangedListener(
                    new NumberPicker.OnValueChangeListener() {
                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            // Allow user to book this appointment.
                            enableBookButton();
                            // Update displays.
                            timeTime = appointments[newVal];
                            mSetTime.setText(timeTime);
                        }
                    }
            );

            //layout to display number picker pop up
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
            RelativeLayout.LayoutParams numPickerParams =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            linearLayout.setLayoutParams(params);
            linearLayout.addView(myNumberPicker,numPickerParams);

            //create a dialog to display picker
            AlertDialog.Builder alertBuild = new AlertDialog.Builder(BookingSystem.this, TimePickerDialog.THEME_HOLO_LIGHT);
            alertBuild.setTitle("Select the time of your appointment:");
            alertBuild.setView(linearLayout);
            alertBuild
                    .setCancelable(false)
                    .setPositiveButton("Select",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    System.out.println("Value Selected: " + myNumberPicker.getValue());

                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertBuild.create();
            alertDialog.show();

        } catch (ParseException | NetworkException e) {
            // Notify the user of any errors.
            NotificationHandler.NetworkErrorDialog(BookingSystem.this);
        }
    }

    public void createDatePicker() {
        // Create a new dialogue.
        dp = new DatePickerDialog(BookingSystem.this, TimePickerDialog.THEME_HOLO_LIGHT, date,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        Date newdate = c.getTime();

        // Set specific options.
        dp.getDatePicker().setMinDate(newdate.getTime());
        dp.getDatePicker().setMaxDate(newdate.getTime() + 604800000);
    }


    /**
     * Prompt user to select a date
     * fired by calling element.
     */
    public void dateOnClick(View v) {

        dp.show();
    }

    /**
     * Provide a subroutine for XML-directed
     * radio-group buttons to fire to log their changes.
     *
     * @param rb  The radio button clicked.
     */
    public void selectGender(View rb) {

        // Log state of radio button.
        boolean checked = ((RadioButton) rb).isChecked();

        // Depending on which button was selected...
        switch (rb.getId()) {

            // Log preference as female advisor.
            case R.id.booking_radio_woman:
                if(checked) {
                    pref = "woman";
                }
                break;
            // Log preference as male advisor.
            case R.id.booking_radio_man:
                if(checked) {
                    pref = "man";
                }
                break;
            // Log preference as any advisor.
            case R.id.booking_radio_nopreference:
                if(checked) {
                    pref = "either";
                }
                break;
        }
    }

    /* Other database methods. */

    /**
     * Submit the user-constructed
     * appointment to the booking system.
     */
    public void postBooking() {

        // Get data from active booking view.
        String date = mSetDate.getText().toString();
        String time = mSetTime.getText().toString();
        String newDate = null;

        // Format it correctly (for us Brits).
        SimpleDateFormat initial = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat needed = new SimpleDateFormat("yyyy-MM-dd");


        // Attempt to log the appointment in the database.
        try {
            newDate = needed.format(initial.parse(date));
            // Remember preferences.
            UtilityManager.getDbUtility(this).addAppointment(time, newDate, pref);
            // Provide visual feedback.
            Toast.makeText(BookingSystem.this, "Appointment Booked", Toast.LENGTH_SHORT).show();
        } catch(ParseException | NetworkException e){
            // Notify the user of any errors.
            NotificationHandler.NetworkErrorDialog(BookingSystem.this);
        }
    }
}
