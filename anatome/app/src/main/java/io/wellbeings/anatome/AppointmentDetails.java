package io.wellbeings.anatome;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Provide visual display of stored user appointment
 * to remind the user, and also disable them from
 * adding multiple appointments.
 *
 * @author Team WellBeings - Bettina, Callum, Josh
 */
public class AppointmentDetails extends AppCompatActivity {

    private Button mBackFromBooked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_details);

        // Set font.
        Typeface customFont = UtilityManager.getThemeUtility(this).getFont("Bariol");

        // Find and save references to UI element.
        mBackFromBooked = (Button)findViewById(R.id.booking_booked_back);

        // Set font.
        mBackFromBooked.setTypeface(customFont);

        // Add back button.
        mBackFromBooked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainScroll.class);
                startActivity(intent);
            }
        });

        // Set back button text.
        mBackFromBooked.setText(
                UtilityManager.getContentLoader(this).getButtonText("back")
        );

        Context ctx = AppointmentDetails.this;
        HashMap<String, String> appointments;

        // Retrieve and display appointment.
        try {

            // Appointment retrieved from database.
            appointments = UtilityManager.getDbUtility(this).getAppointment();

            // References to UI elements.
            TextView timeView = (TextView) findViewById(R.id.booking_booked_time);
            TextView dateView = (TextView) findViewById(R.id.booking_booked_date);

            // Appointment details saved and converted to Strings.
            String date = appointments.get("App_Date").toString();
            String time = appointments.get("App_Time").toString();

            // Text of UI elements displays appointment details.
            dateView.setText(date);
            timeView.setText(time);

            // Create a notification containing appointment details.
            NotificationHandler.pushNotification(AppointmentDetails.this,
                    "Booked Appointment:", "Time: " + time + " - Date: " + date);

        } catch(NetworkException e) {
            // Exception caught in case of Network Failure.
            NotificationHandler.NetworkErrorDialog(AppointmentDetails.this);
        }

    }
}