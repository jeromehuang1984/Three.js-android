package edu.three.core;

import java.util.HashMap;

public class Event {
    public String type = "evt";
    public EventDispatcher target;
    public Object data;
    public HashMap map;

    public Event(String type) {
        this.type = type;
    }
}
