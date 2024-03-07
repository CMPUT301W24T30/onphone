package com.example.scanpal;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Base class for events, contains all event data.
 */
public class Event {

    private String id; // will be generated using UUID
    private String name;
    private String description;
    private User organizer;
    private String location;
    private ArrayList<Attendee> participants;
    private String signUpAddress;
    private String infoAddress;
    private Bitmap qrToEvent;
    private Bitmap qrToCheckIn;
    private String imageUrl;

    /**
     * Constructs an event with an organizer, name, and description.
     *
     * @param organizer   The user who organized the event.
     * @param name        The name of the event.
     * @param description The description of the event.
     * @param imageUrl    The url to the firebase stored image.
     */
    public Event(User organizer, String name, String description, String imageUrl) {
        this.organizer = organizer;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        participants = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public ArrayList<Attendee> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Attendee> participants) {
        this.participants = participants;
    }

    public String getSignUpAddress() {
        return signUpAddress;
    }

    public void setSignUpAddress(String signUpAddress) {
        this.signUpAddress = signUpAddress;
    }

    public String getInfoAddress() {
        return infoAddress;
    }

    public void setInfoAddress(String infoAddress) {
        this.infoAddress = infoAddress;
    }

    public Bitmap getQrToEvent() {
        return qrToEvent;
    }

    public void setQrToEvent(Bitmap qrToEvent) {
        this.qrToEvent = qrToEvent;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public Bitmap getQrToCheckIn() {
        return qrToCheckIn;
    }

    public void setQrToCheckIn(Bitmap qrToCheckIn) {
        this.qrToCheckIn = qrToCheckIn;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}