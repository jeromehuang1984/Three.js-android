package edu.three.core;

import java.util.ArrayList;
import java.util.HashMap;

public class EventDispatcher {
    public HashMap<String, ArrayList<IListener>> listeners = new HashMap<>();

    public void addEventListener(String type, IListener listener) {
       if (!listeners.containsKey(type)) {
           listeners.put(type, new ArrayList<IListener>());
       }
       if (listeners.get(type).indexOf(listener) < 0) {
           listeners.get(type).add(listener);
       }
    }

    public boolean hasEventListener(String type, IListener listener) {
        return listeners.get(type) != null && listeners.get(type).indexOf(listener) >= 0;
    }

    public boolean removeEventListener(String type, IListener listener) {
        ArrayList<IListener> lst = listeners.get(type);
        if (lst != null) {
            return lst.remove(listener);
        }
        return false;
    }

    public boolean removeListeners(String type) {
        if (listeners.containsKey(type)) {
            listeners.remove(type);
            return true;
        }
        return false;
    }

    public void dispatchEvent(Event event) {
        ArrayList<IListener> lst = listeners.get(event.type);
        if (lst != null) {
            event.target = this;
            for (int i = 0; i < lst.size(); i++) {
                lst.get(i).onEvent(event);
            }
        }
    }
}
