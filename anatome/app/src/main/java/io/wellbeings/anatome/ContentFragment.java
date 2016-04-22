package io.wellbeings.anatome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.client.cache.Resource;

/**
 *  This class handles the loading of section information.
 */
public class ContentFragment extends Fragment implements Widget {

    // Store content view.
    View view;

    // Store section name.
    private String section;

    public ContentFragment() {
        // Required empty public constructor
    }

    public static ContentFragment newInstance() {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_content, container, false);
        section = getArguments().getString("section");

        // Populate text containers with informative content.
        populateContent();

        // Load user tips from the database.
        loadComments();

        return view;
    }

    // Populate frame UI components with local information.
    private void populateContent() {

        /* Style background elements. */

        ((LinearLayout) view.findViewById(R.id.content_container)).setBackgroundColor(
                ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_bg"))
        );

        /* Sequentially populate and style pre-determined key sections of content. */

        ((TextView) view.findViewById(R.id.content_title)).setText(
                UtilityManager.getContentLoader(getContext()).getHeaderText(section, "title"));
        ((TextView) view.findViewById(R.id.content_title)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_text"))
        );

        ((TextView) view.findViewById(R.id.content_titlecontent)).setText(
                UtilityManager.getContentLoader(getContext()).getInfoText(section, "title"));
        ((TextView) view.findViewById(R.id.content_titlecontent)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_text"))
        );

        ((TextView) view.findViewById(R.id.content_tips)).setText(
                UtilityManager.getContentLoader(getContext()).getHeaderText(section, "tips"));
        ((TextView) view.findViewById(R.id.content_tips)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_text"))
        );

        ((TextView) view.findViewById(R.id.content_tipscontent)).setText(
                UtilityManager.getContentLoader(getContext()).getInfoText(section, "tips"));
        ((TextView) view.findViewById(R.id.content_tipscontent)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_text"))
        );

        ((TextView) view.findViewById(R.id.content_links)).setText(
                UtilityManager.getContentLoader(getContext()).getHeaderText(section, "links"));
        ((TextView) view.findViewById(R.id.content_links)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_main_text"))
        );

        // Populate links, make them clickable.
        ((TextView) view.findViewById(R.id.content_linkscontent)).setText(Html.fromHtml(
                UtilityManager.getContentLoader(getContext()).getLinks(section)));
        ((TextView) view.findViewById(R.id.content_linkscontent)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.content_linkscontent)).setTextColor(ContextCompat.getColor(getContext(),
                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_accent_text"))
        );

        // Allow user to submit their own comment.
        ((Button) view.findViewById(R.id.content_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create comment input.
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle("Share your advice!");
                builder.setCancelable(true);
                final EditText commentInput = new EditText(getContext());

                // Force maximum length.
                commentInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(
                        UtilityManager.getUserUtility(getContext()).getCOMMENT_LENGTH()
                )});
                builder.setView(commentInput);

                // Dictate how the buttons should look.
                builder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(commentInput.getText().toString().length() < 10) {
                            Toast.makeText(getContext(), "Sorry, comment too short!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            try {
                                UtilityManager.getDbUtility(getContext()).addComment(
                                        commentInput.getText().toString(),
                                        section,
                                        UtilityManager.getUserUtility(getContext()).getName()
                                );
                            } catch (NetworkException e) {
                                Toast.makeText(getContext(), "Sorry, there was an error!",
                                        Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getContext(), "Awesome, it's pending approval!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();

            }
        });

        // Log when the content was last updated.
        ((TextView) view.findViewById(R.id.content_modified)).setText(
                UtilityManager.getContentLoader(getContext()).getButtonText("date") + " " +
                        UtilityManager.getContentLoader(getContext()).getDateModified(section)
        );

        /* Load graphical element. */

        final int resourceIdLarge = getResources().getIdentifier(
                section + "_graphic", "drawable", getContext().getPackageName()
        );
        ((ImageView) view.findViewById(R.id.content_graphic_other)).setImageResource(resourceIdLarge);

    }

    // Load custom social aspect of app via comments.
    private void loadComments() {

        try {

            // Get outer container.
            LinearLayout ll = (LinearLayout) view.findViewById(R.id.content_comments_container);

            // Check for intentional network impediment.
            if(UtilityManager.getUserUtility(getContext()).isNetwork()) {

                // Retrieve user comments.
                HashMap<String, String> comments = UtilityManager.getDbUtility(getContext()).getComments(section);

                // Create visual element for every comment.
                if (comments != null) {

                    for (String comment : comments.keySet()) {

                        // Set and style comment content.
                        TextView commentView = new TextView(getContext());
                        commentView.setText(comment);
                        commentView.setTextColor(ContextCompat.getColor(getContext(),
                                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_secondary_bg"))
                        );
                        commentView.setTextSize(18);
                        commentView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        // Add element.
                        ll.addView(commentView);

                        // Set and style author content.
                        TextView authorView = new TextView(getContext());
                        authorView.setText("- " + comments.get(comment));
                        authorView.setTextColor(ContextCompat.getColor(getContext(),
                                        UtilityManager.getThemeUtility(getContext()).getColour(section + "_accent_text"))
                        );
                        authorView.setTextSize(16);
                        authorView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        // Add element.
                        ll.addView(authorView);

                        // Add separator with margin.
                        View v = new View(getContext());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                5
                        );
                        lp.setMargins(0, 20, 0, 20);
                        v.setLayoutParams(lp);
                        v.setBackgroundColor(ContextCompat.getColor(getContext(),
                                UtilityManager.getThemeUtility(getContext()).getColour(section + "_accent_text")));
                        ll.addView(v);

                    }

                }
                else {

                    // Otherwise remove the unnecessary views.
                    ll.setVisibility(View.INVISIBLE);

                }

            }
        } catch (NetworkException e) {

            // Display unintended network error.
            NotificationHandler.NetworkErrorDialog(getContext());

        }

    }

}