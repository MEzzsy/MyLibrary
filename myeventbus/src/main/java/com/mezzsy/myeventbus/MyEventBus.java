package com.mezzsy.myeventbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MyEventBus {
    private HashMap<Class<?>, ArrayList<Observation>> mObserverArrayListHashMap;

    private MyEventBus() {
        mObserverArrayListHashMap = new HashMap<>();
    }

    public static MyEventBus getDefault() {
        return SingletonHolder.SINGLETON;
    }

    public void register(Object observer) {
        Class<?> observerClass = observer.getClass();
        List<ObserverMethod> observerMethods = findObserverMethods(observerClass);
        for (ObserverMethod observerMethod : observerMethods) {
            observe(observer, observerMethod);
        }
    }

    private void observe(Object observer, ObserverMethod observerMethod) {
        Class<?> eventType = observerMethod.getEventType();
        Observation observation = new Observation(observer, observerMethod);
        ArrayList<Observation> observations = mObserverArrayListHashMap.get(eventType);
        if (observations == null) {
            observations = new ArrayList<>();
            mObserverArrayListHashMap.put(eventType, observations);
        } else {
            if (observations.contains(observation)) {
                return;
            }
        }
        observations.add(observation);
    }

    private List<ObserverMethod> findObserverMethods(Class<?> observerClass) {
        List<ObserverMethod> observerMethods = new ArrayList<>();
        for (Method method : observerClass.getDeclaredMethods()) {
            Observe observe = method.getAnnotation(Observe.class);
            if (observe != null) {
                Class<?> eventType = method.getParameterTypes()[0];
                ObserverMethod observerMethod = new ObserverMethod(method, eventType);
                observerMethods.add(observerMethod);
            }
        }
        return observerMethods;
    }

    public void post(Object event) {
        Class<?> eventType = event.getClass();
        ArrayList<Observation> observations = mObserverArrayListHashMap.get(eventType);
        if (observations == null) {
            return;
        }
        for (Observation observation : observations) {
            Object observer = observation.getObserver();
            Method method = observation.getObserverMethod().getMethod();
            try {
                method.invoke(observer, event);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private static class SingletonHolder {
        private static final MyEventBus SINGLETON = new MyEventBus();
    }
}
