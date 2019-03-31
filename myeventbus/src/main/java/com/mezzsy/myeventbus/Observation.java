package com.mezzsy.myeventbus;

class Observation {
    private Object observer;
    private ObserverMethod observerMethod;

    public Observation(Object observer, ObserverMethod observerMethod) {
        this.observer = observer;
        this.observerMethod = observerMethod;
    }

    public Object getObserver() {
        return observer;
    }

    public ObserverMethod getObserverMethod() {
        return observerMethod;
    }
}
