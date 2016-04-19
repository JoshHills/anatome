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
 * Created by bettinaalexieva on 15/03/2016.
 */
public class BookingDetails extends AppCompatActivity {

    private Button mBackFromBooked;
    private TextView mTimeBooked, mDateBooked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_details_layout);

        Typeface customFont = defineCustomFont();

        mBackFromBooked = (Button)findViewById(R.id.backFromBooked);
        mBackFromBooked.setTypeface(customFont);

        mTimeBooked = (TextView) findViewById(R.id.bookedTime);
        mTimeBooked.setTypeface(customFont);

        mDateBooked = (TextView) findViewById(R.id.bookedDate);
        mDateBooked.setTypeface(customFont);

        mBackFromBooked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainScroll.class);
                startActivity(intent);
            }
        });

        Context ctx = BookingDetails.this;
        HashMap<String, String> appointments;

        appointments = UtilityManager.getDbUtility(this).getAppointment();

        TextView timeView = (TextView)findViewById(R.id.bookedTime);
        TextView dateView = (TextView)findViewById(R.id.bookedDate);

<<<<<<< HEAD:anatome/app/src/main/java/io/wellbeings/anatome/BookingDetails.java
        dateView.setText(appointments.get("App_Date").toString());
        timeView.setText(appointments.get("App_Time").toString());
=======
        String date = appointments.get("App_Date").toString();
        String time = appointments.get("App_Time").toString();

        dateView.setText(date);
        timeView.setText(time);

        NotificationHandler.pushNotification(TestLayout.this, "Booked Appointment:", "Time: " + time + "/nDate: " + date);

>>>>>>> master:anatome/app/src/main/java/io/wellbeings/anatome/TestLayout.java
    }

    private Typeface defineCustomFont() {

        AssetManager assetManager = getAssets();
        Typeface customFont = Typeface.createFromAsset(assetManager, "fonts/champagne.ttf");

        return customFont;
    }
}
