package com.example.proect_lab123.util.observer;

import com.example.proect_lab123.util.event.Event;

public interface Observable<E extends Event> {
    void addObserver(Observer<E> e);
    void removeObserver(Observer<E> e);
    void notifyObservers(E e);
}
