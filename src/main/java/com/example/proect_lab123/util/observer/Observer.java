package com.example.proect_lab123.util.observer;

import com.example.proect_lab123.util.event.Event;

public interface Observer<E extends Event> {
    void update(E event);
}
