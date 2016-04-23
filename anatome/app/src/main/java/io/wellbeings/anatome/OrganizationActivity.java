package io.wellbeings.anatome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Security;

/**
 * Display 'plugged-in' organization's
 * information to guide user to extra help.
 */
public class OrganizationActivity extends FragmentActivity implements OnMapReadyCallback {

    // Map-related private fields.
    private LatLng orgLocation;
    private final float ZOOM_LEVEL = 18.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Hide intrusive android status bars.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        super.onCreate(savedInstanceState);

        // Load the corresponding view.
        setContentView(R.layout.activity_organization);

        populateContent();

        initGUI();

        // Create the inner map fragment.
        if(orgLocation != null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.organization_map);
            mapFragment.getMapAsync(this);
        }
    }

    private void initGUI() {

        // Exit button.
        ((ImageButton)findViewById(R.id.organization_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrganizationActivity.this, MainScroll.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_bottom_out);
            }
        });

        // Set the background of the layout container.
        Glide.with(this).load(R.drawable.organization_bg)
                .dontTransform()
                .override(getResources().getDisplayMetrics().widthPixels,
                        getResources().getDisplayMetrics().heightPixels)
                .into((ImageView) findViewById(R.id.organization_bg));


    }

    private void populateContent() {

        // If internet not allowed...
        if(!UtilityManager.getUserUtility(this).isNetwork()) {
            // Close activity.
            NotificationHandler.networkErrorDialog(this);
            finish();
        }

        try {

            // Attempt to retrieve the organization's location.
            orgLocation = UtilityManager.getDbUtility(this).getLatLong();

        /* Set textual content. */

            // Set organization name.
            ((TextView) findViewById(R.id.organization_name)).setText(
                    UtilityManager.getDbUtility(this).getOrgName()
            );

            // Set organization description.
            ((TextView) findViewById(R.id.organization_description)).setText(
                    UtilityManager.getDbUtility(this).getOrgDescription()
            );
        }catch(NetworkException e) {
            NotificationHandler.networkErrorDialog(OrganizationActivity.this);
        }


    }

    /**
     * Implement customized map set-up.
     *
     * @param map Reference to the physical map element.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        try {
            // Style the map.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Add location services.
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException s) {
            }

            // Allow the user to pinch around to see surroundings.
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(true);

            // Set pin to organization's location and style it.
            String pinName = UtilityManager.getDbUtility(this).getOrgName();
            if (pinName != null) {
                Marker orgMarker = map.addMarker(new MarkerOptions().position(orgLocation));
            } else {
                Marker orgMarker = map.addMarker(new MarkerOptions().position(orgLocation));
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(orgLocation, ZOOM_LEVEL));
        }catch (NetworkException e) {
            NotificationHandler.networkErrorDialog(OrganizationActivity.this);
        }
    }

}