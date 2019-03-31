package com.mezzsy.myeventbus;

import java.lang.reflect.Method;

class ObserverMethod {
    private Method mMethod;
    private Class<?> mEventType;

    public ObserverMethod(Method method, Class<?> eventType) {
        mMethod = method;
        this.mEventType = eventType;
    }

    public Method getMethod() {
        return mMethod;
    }

    public Class<?> getEventType() {
        return mEventType;
    }
}
