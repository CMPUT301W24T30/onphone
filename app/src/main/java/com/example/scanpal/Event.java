package com.example.scanpal;

/**
 * Base class for events, contains all event data.
 */
public class Event {

    private String id; // Autogenerated when saved to the database
    private String name;
    private String description;
    private User organizer;
    private Attendee[] participants;
    private String signUpAddress;
    private String infoAddress;

    /**
     * Constructs an event with an organizer, name, and description.
     *
     * @param organizer   The user who organized the event.
     * @param name        The name of the event.
     * @param description The description of the event.
     */
    public Event(User organizer, String name, String description) {
        this.organizer = organizer;
        this.name = name;
        this.description = description;
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

    public Attendee[] getParticipants() {
        return participants;
    }

    public void setParticipants(Attendee[] participants) {
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
}
