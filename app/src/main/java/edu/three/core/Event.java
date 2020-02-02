package edu.three.core;

public class Event {
    public String type = "evt";
    public EventDispatcher target;

    public Event(String type) {
        this.type = type;
    }
}
