package com.example.scanpal;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Objects;
import android.content.Intent;
import com.example.scanpal.MapsActivity;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment for displaying event details. It expects an event ID as an argument
 * and uses it to fetch and display event details from a Firestore database.
 */
public class EventDetailsFragment extends Fragment {

    // User details of the current user
    public User userDetails;
    // The attendee of the event
    public User user;
    public Attendee attendee;
    // Attendee ID, constructed from user's username and eventID
    public String attendeeId;
    public AttendeeController attendeeController;
    // ActivityResultLauncher for QR code scanner
    ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;
    // Event details
    private String eventName;
    private String eventDescription;
    private String eventOrganizer;
    private String getEventOrganizerUserName;
    private String eventLocation;
    private ImageView eventPoster;
    private String ImageURI;
    private Button joinButton;
    private ImageView organizerImage;

    /**
     * Default constructor for EventDetailsFragment.
     */
    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if userDetails is not null and arguments are provided
        if (userDetails != null && getArguments() != null) {
            String eventID = getArguments().getString("0");
            if (eventID != null) {
                attendeeId = userDetails.getUsername() + eventID;
                UpdateUI(attendeeId);
            } else {
                Log.e("EventDetailsFragment", "Event ID is null in onResume.");
            }
        } else {
            Log.e("EventDetailsFragment", "User details are null or getArguments() is null in onResume.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_details, null, false);

        MaterialButton mapButton = view.findViewById(R.id.map_button);
        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            String eventID = getArguments().getString("event_id");
            intent.putExtra("event_id", eventID);
            startActivity(intent);
        });

        // Retrieve and display event details
        assert getArguments() != null;
        String eventID = getArguments().getString("event_id");
        fetchEventDetails(eventID);

        // Retrieve and display event details
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (eventID != null) {
                fetchEventDetails(eventID);
            } else {
                Log.e("EventDetailsFragment", "Event ID is null.");
                Toast.makeText(getContext(), "Error: Event details cannot be loaded.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e("EventDetailsFragment", "Arguments are null.");
            // Handling the null case
            Toast.makeText(getContext(), "Error: Event details cannot be loaded.", Toast.LENGTH_LONG).show();
        }

        // Initialize UI components and setup event handlers
        FloatingActionButton backButton = view.findViewById(R.id.event_details_backButton);
        eventPoster = view.findViewById(R.id.event_detail_imageView);
        joinButton = view.findViewById(R.id.join_button);
        FloatingActionButton eventEditButton = view.findViewById(R.id.event_editButton);
        organizerImage = view.findViewById(R.id.organizer_image);
        FloatingActionButton scanQR = view.findViewById(R.id.scan_code);
        FloatingActionButton profileButton = view.findViewById(R.id.button_profile);

        // Setup user and attendee controllers
        UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
        qrScannerController = new QrScannerController(attendeeController);

        // Fetch and setup current user details
        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                userDetails = user;
                attendeeId = userDetails.getUsername() + eventID;
                attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
                    @Override
                    public void onSuccess(Attendee existingAttendee) {
                        Log.d("EventDetailsFragment", "Attendee already exists. No need to add a new one.");
                        attendee = existingAttendee;
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("EventDetailsFragment", "Attendee does not exist. Adding a new one.");

                        if (eventID != null && userDetails != null) {
                            attendee = new Attendee(userDetails, eventID, false, false);
                            attendee.setId(attendeeId);
                            attendeeController.addAttendee(attendee, new AttendeeAddCallback() {
                                @Override
                                public void onSuccess(Attendee attendee) {
                                    Log.d("EventDetailsFragment", "New attendee added");
                                    onResume();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(view.getContext(), "Failed to add attendee", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to load user details", Toast.LENGTH_LONG).show();
            }
        });

        // Setup QR code scanner
        qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String username = userController.fetchStoredUsername();
                qrScannerController.handleResult(result.getContents(), username);
            } else {
                Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        });
        scanQR.setOnClickListener(v -> qrCodeScanner.launch(QrScannerController.getOptions()));

        // Navigate back to the events page
        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigate(R.id.eventsPage);
        });

        // Navigate to the user profile page
        profileButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);
            navController.navigate(R.id.event_details_to_profile);
        });

        // Implement event edit functionality
        eventEditButton.setOnClickListener(v -> {
            // Check if the current user is the organizer or an admin
            if (userDetails.getUsername().equals(getEventOrganizerUserName) ||
                    userDetails.isAdministrator()) {
                // Navigate to the event edit page with necessary details
                NavController navController = NavHostFragment.findNavController(EventDetailsFragment.this);

                Bundle bundle = new Bundle();
                bundle.putString("0", eventID);
                bundle.putString("1", eventName);
                bundle.putString("2", eventLocation);
                bundle.putString("3", eventDescription);
                bundle.putString("4", ImageURI);

                navController.navigate(R.id.edit_existing_event, bundle);
            } else {
                Toast.makeText(view.getContext(), "You Cannot Edit This Event", Toast.LENGTH_LONG).show();
            }
        });

        // Implement join button functionality
        joinButton.setOnClickListener(v -> {
            if (attendee != null) {
                attendee.setRsvp(!attendee.isRsvp());
                attendeeController.updateAttendee(attendee, new AttendeeUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        setJoinButton();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(view.getContext(), "Failed to update RSVP status", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Log.e("EventDetailsFragment", "Join button clicked but attendee is null.");
            }
        });

        return view;
    }

    /**
     * This method takes the inputted /User document reference stores their first
     * and
     * lastname as one string in the 'eventOrganizer' field
     *
     * @param userRef this is just the specific 'user' that was found to be tied to
     *                the event
     */
    private void fetchOrganizer(DocumentReference userRef) {

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot organizerDoc = task.getResult();
                if (organizerDoc.exists()) {
                    String firstName = organizerDoc.getString("firstName");
                    String lastName = organizerDoc.getString("lastName");
                    String profileImage = organizerDoc.getString("photo");

                    String organizerName = firstName + " " + lastName;
                    getEventOrganizerUserName = organizerDoc.getId();
                    eventOrganizer = organizerName;
                    Glide.with(EventDetailsFragment.this)
                            .load(profileImage)
                            .apply(new RequestOptions().circleCrop())
                            .into(organizerImage);

                    // 'isAdded' is necessary for async task purposes
                    if (isAdded()) {
                        TextView OrganizerName = requireView().findViewById(R.id.event_orgName);

                        if (OrganizerName != null) {
                            OrganizerName.setText(eventOrganizer);
                        }
                    }
                } else {
                    Log.d("TAG", "Organizer document does not exist");
                }
            } else {
                Log.d("TAG", "Failed to get organizer document", task.getException());
            }
        });
    }

    /**
     * Fetch and display event details from Firestore using the provided event ID.
     *
     * @param eventID The ID of the event.
     */
    void fetchEventDetails(String eventID) {
        EventController eventController = new EventController();
        FirebaseFirestore db = eventController.getDatabase();

        CollectionReference eventCollection = db.collection("Events");
        DocumentReference EventDocument = eventCollection.document(eventID);

        EventDocument.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    eventName = document.getString("name");
                    eventDescription = document.getString("description");
                    eventLocation = document.getString("location");
                    ImageURI = document.getString("photo");

                    //since user technically another document
                    fetchOrganizer(Objects.requireNonNull(document.getDocumentReference("organizer")));

                    if (isAdded()) {
                        TextView eventTitle = requireView().findViewById(R.id.event_Title);
                        TextView OrganizerName = requireView().findViewById(R.id.event_orgName);
                        TextView eventDes = requireView().findViewById(R.id.event_description);
                        TextView eventLoc = requireView().findViewById(R.id.event_Location);

                        if (eventTitle != null) {
                            eventTitle.setText(eventName);
                        }
                        if (eventDes != null) {
                            eventDes.setText(eventDescription);
                        }
                        if (eventLoc != null) {
                            eventLoc.setText(eventLocation);
                        }
                        if (eventPoster != null) {
                            Uri imageURI = Uri.parse(ImageURI);
                            Glide.with(requireView())
                                    .load(imageURI)
                                    .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background))
                                    .into(eventPoster);
                        }
                    }

                } else {

                    // Document does not exist

                    Toast.makeText(requireView().getContext(), "Event Doesn't exist ", Toast.LENGTH_LONG).show();
                    //TODO: navigate back to / stay on events Page then
                }
            } else {
                // Error getting document
                Toast.makeText(requireView().getContext(), "Error retrieving Event", Toast.LENGTH_LONG).show();
                //TODO: navigate back to / stay on events Page then

            }
        });
    }

    /**
     * Updates the UI based on the attendee's RSVP status.
     */
    private void setJoinButton() {
        if (attendee != null && attendee.isRsvp()) {
            joinButton.setText("Cancel RSVP");
            joinButton.setBackgroundColor(Color.RED);
        } else {
            joinButton.setText("Join Event");
            joinButton.setBackgroundColor(Color.GREEN);
        }
    }

    /**
     * Update UI elements based on attendee information.
     *
     * @param attendeeId The ID of the attendee.
     */
    private void UpdateUI(String attendeeId) {
        attendeeController.fetchAttendee(attendeeId, new AttendeeFetchCallback() {
            @Override
            public void onSuccess(Attendee existingAttendee) {
                attendee = existingAttendee;
                setJoinButton();
            }

            @Override
            public void onError(Exception e) {
                Log.e("EventDetailsFragment", "Failed to fetch attendee: ", e);
            }
        });
    }
}
