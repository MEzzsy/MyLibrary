# 仿EventBus实现

EventBus的分析：https://www.jianshu.com/p/84a8c438d0d3，这是我自己的分析，写的十分简（la）洁（ji）。

这个仿EventBus也主要是实现了EventBus中的最基本的功能，延迟和子线程等功能都没有去实现。

## 构造单例对象

```
private HashMap<Class<?>, ArrayList<Observation>> mObserverArrayListHashMap;

private MyEventBus() {
    mObserverArrayListHashMap = new HashMap<>();
}

public static MyEventBus getDefault() {
    return SingletonHolder.SINGLETON;
}

private static class SingletonHolder {
        private static final MyEventBus SINGLETON = new MyEventBus();
    }
```

这里我采用静态内部类的方式实现单例模式

## 注册

```
public void register(Object observer) {
    Class<?> observerClass = observer.getClass();
    List<ObserverMethod> observerMethods = findObserverMethods(observerClass);
    for (ObserverMethod observerMethod : observerMethods) {
        observe(observer, observerMethod);
    }
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
```

### 两个封装类

```
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
```

## 发送

```
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
```

## 测试

观察者

```
public class MyObserver1 {
    public MyObserver1() {
        MyEventBus.getDefault().register(this);
    }

    @Observe
    public void myEvent(MyEvent event){
        System.out.println("我是观察者1，我观察到了");
    }
}

public class MyObserver2 {
    public MyObserver2() {
        MyEventBus.getDefault().register(this);
    }

    @Observe
    public void myEvent(MyEvent event){
        System.out.println("我是观察者2，我观察到了");
    }
}
```

事件

```
public class MyEvent {
}
```

被观察者及测试

```
public class MyObserved {
    public MyObserved() {
        MyObserver1 observer1 = new MyObserver1();
        MyObserver2 observer2 = new MyObserver2();
    }

    public static void main(String[] args) {
        MyObserved observed = new MyObserved();
        Scanner in = new Scanner(System.in);
        while (true) {
            int code = in.nextInt();
            if (code == 1) {
                observed.sendEvent();
            } else {
                break;
            }
        }
    }

    public void sendEvent() {
        MyEvent event = new MyEvent();
        MyEventBus.getDefault().post(event);
    }
}

output:
1
我是观察者1，我观察到了
我是观察者2，我观察到了
1
我是观察者1，我观察到了
我是观察者2，我观察到了
2

Process finished with exit code 0
```

