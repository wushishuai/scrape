package p006me.goldze.mvvmhabit.bus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import p006me.goldze.mvvmhabit.binding.command.BindingAction;
import p006me.goldze.mvvmhabit.binding.command.BindingConsumer;

/* renamed from: me.goldze.mvvmhabit.bus.Messenger */
/* loaded from: classes.dex */
public class Messenger {
    private static Messenger defaultInstance;
    private HashMap<Type, List<WeakActionAndToken>> recipientsOfSubclassesAction;
    private HashMap<Type, List<WeakActionAndToken>> recipientsStrictAction;

    /* renamed from: me.goldze.mvvmhabit.bus.Messenger$NotMsgType */
    /* loaded from: classes.dex */
    public static class NotMsgType {
    }

    public static Messenger getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new Messenger();
        }
        return defaultInstance;
    }

    public static void overrideDefault(Messenger messenger) {
        defaultInstance = messenger;
    }

    public static void reset() {
        defaultInstance = null;
    }

    public void register(Object obj, BindingAction bindingAction) {
        register(obj, (Object) null, false, bindingAction);
    }

    public void register(Object obj, boolean z, BindingAction bindingAction) {
        register(obj, (Object) null, z, bindingAction);
    }

    public void register(Object obj, Object obj2, BindingAction bindingAction) {
        register(obj, obj2, false, bindingAction);
    }

    public void register(Object obj, Object obj2, boolean z, BindingAction bindingAction) {
        HashMap<Type, List<WeakActionAndToken>> hashMap;
        List<WeakActionAndToken> list;
        if (z) {
            if (this.recipientsOfSubclassesAction == null) {
                this.recipientsOfSubclassesAction = new HashMap<>();
            }
            hashMap = this.recipientsOfSubclassesAction;
        } else {
            if (this.recipientsStrictAction == null) {
                this.recipientsStrictAction = new HashMap<>();
            }
            hashMap = this.recipientsStrictAction;
        }
        if (!hashMap.containsKey(NotMsgType.class)) {
            list = new ArrayList<>();
            hashMap.put(NotMsgType.class, list);
        } else {
            list = hashMap.get(NotMsgType.class);
        }
        list.add(new WeakActionAndToken(new WeakAction(obj, bindingAction), obj2));
        cleanup();
    }

    public <T> void register(Object obj, Class<T> cls, BindingConsumer<T> bindingConsumer) {
        register(obj, null, false, bindingConsumer, cls);
    }

    public <T> void register(Object obj, boolean z, Class<T> cls, BindingConsumer<T> bindingConsumer) {
        register(obj, null, z, bindingConsumer, cls);
    }

    public <T> void register(Object obj, Object obj2, Class<T> cls, BindingConsumer<T> bindingConsumer) {
        register(obj, obj2, false, bindingConsumer, cls);
    }

    public <T> void register(Object obj, Object obj2, boolean z, BindingConsumer<T> bindingConsumer, Class<T> cls) {
        HashMap<Type, List<WeakActionAndToken>> hashMap;
        List<WeakActionAndToken> list;
        if (z) {
            if (this.recipientsOfSubclassesAction == null) {
                this.recipientsOfSubclassesAction = new HashMap<>();
            }
            hashMap = this.recipientsOfSubclassesAction;
        } else {
            if (this.recipientsStrictAction == null) {
                this.recipientsStrictAction = new HashMap<>();
            }
            hashMap = this.recipientsStrictAction;
        }
        if (!hashMap.containsKey(cls)) {
            list = new ArrayList<>();
            hashMap.put(cls, list);
        } else {
            list = hashMap.get(cls);
        }
        list.add(new WeakActionAndToken(new WeakAction(obj, bindingConsumer), obj2));
        cleanup();
    }

    private void cleanup() {
        cleanupList(this.recipientsOfSubclassesAction);
        cleanupList(this.recipientsStrictAction);
    }

    public void sendNoMsg(Object obj) {
        sendToTargetOrType(null, obj);
    }

    public void sendNoMsgToTarget(Object obj) {
        sendToTargetOrType(obj.getClass(), null);
    }

    public void sendNoMsgToTargetWithToken(Object obj, Object obj2) {
        sendToTargetOrType(obj2.getClass(), obj);
    }

    public <T> void send(T t) {
        sendToTargetOrType(t, null, null);
    }

    public <T> void send(T t, Object obj) {
        sendToTargetOrType(t, null, obj);
    }

    public <T, R> void sendToTarget(T t, R r) {
        sendToTargetOrType(t, r.getClass(), null);
    }

    public void unregister(Object obj) {
        unregisterFromLists(obj, this.recipientsOfSubclassesAction);
        unregisterFromLists(obj, this.recipientsStrictAction);
        cleanup();
    }

    public <T> void unregister(Object obj, Object obj2) {
        unregisterFromLists(obj, obj2, (BindingAction) null, this.recipientsStrictAction);
        unregisterFromLists(obj, obj2, (BindingAction) null, this.recipientsOfSubclassesAction);
        cleanup();
    }

    private static <T> void sendToList(T t, Collection<WeakActionAndToken> collection, Type type, Object obj) {
        if (collection != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(collection);
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                WeakActionAndToken weakActionAndToken = (WeakActionAndToken) it.next();
                WeakAction action = weakActionAndToken.getAction();
                if (action != null && weakActionAndToken.getAction().isLive() && weakActionAndToken.getAction().getTarget() != null && (type == null || weakActionAndToken.getAction().getTarget().getClass() == type || classImplements(weakActionAndToken.getAction().getTarget().getClass(), type))) {
                    if ((weakActionAndToken.getToken() == null && obj == null) || (weakActionAndToken.getToken() != null && weakActionAndToken.getToken().equals(obj))) {
                        action.execute(t);
                    }
                }
            }
        }
    }

    private static void unregisterFromLists(Object obj, HashMap<Type, List<WeakActionAndToken>> hashMap) {
        if (!(obj == null || hashMap == null || hashMap.size() == 0)) {
            synchronized (hashMap) {
                for (Type type : hashMap.keySet()) {
                    for (WeakActionAndToken weakActionAndToken : hashMap.get(type)) {
                        WeakAction action = weakActionAndToken.getAction();
                        if (action != null && obj == action.getTarget()) {
                            action.markForDeletion();
                        }
                    }
                }
            }
            cleanupList(hashMap);
        }
    }

    private static <T> void unregisterFromLists(Object obj, BindingConsumer<T> bindingConsumer, HashMap<Type, List<WeakActionAndToken>> hashMap, Class<T> cls) {
        if (!(obj == null || hashMap == null || hashMap.size() == 0 || !hashMap.containsKey(cls))) {
            synchronized (hashMap) {
                for (WeakActionAndToken weakActionAndToken : hashMap.get(cls)) {
                    WeakAction action = weakActionAndToken.getAction();
                    if (action != null && obj == action.getTarget() && (bindingConsumer == null || bindingConsumer == action.getBindingConsumer())) {
                        weakActionAndToken.getAction().markForDeletion();
                    }
                }
            }
        }
    }

    private static void unregisterFromLists(Object obj, BindingAction bindingAction, HashMap<Type, List<WeakActionAndToken>> hashMap) {
        if (!(obj == null || hashMap == null || hashMap.size() == 0 || !hashMap.containsKey(NotMsgType.class))) {
            synchronized (hashMap) {
                for (WeakActionAndToken weakActionAndToken : hashMap.get(NotMsgType.class)) {
                    WeakAction action = weakActionAndToken.getAction();
                    if (action != null && obj == action.getTarget() && (bindingAction == null || bindingAction == action.getBindingAction())) {
                        weakActionAndToken.getAction().markForDeletion();
                    }
                }
            }
        }
    }

    private static <T> void unregisterFromLists(Object obj, Object obj2, BindingConsumer<T> bindingConsumer, HashMap<Type, List<WeakActionAndToken>> hashMap, Class<T> cls) {
        if (!(obj == null || hashMap == null || hashMap.size() == 0 || !hashMap.containsKey(cls))) {
            synchronized (hashMap) {
                for (WeakActionAndToken weakActionAndToken : hashMap.get(cls)) {
                    WeakAction action = weakActionAndToken.getAction();
                    if (action != null && obj == action.getTarget() && (bindingConsumer == null || bindingConsumer == action.getBindingConsumer())) {
                        if (obj2 == null || obj2.equals(weakActionAndToken.getToken())) {
                            weakActionAndToken.getAction().markForDeletion();
                        }
                    }
                }
            }
        }
    }

    private static void unregisterFromLists(Object obj, Object obj2, BindingAction bindingAction, HashMap<Type, List<WeakActionAndToken>> hashMap) {
        if (!(obj == null || hashMap == null || hashMap.size() == 0 || !hashMap.containsKey(NotMsgType.class))) {
            synchronized (hashMap) {
                for (WeakActionAndToken weakActionAndToken : hashMap.get(NotMsgType.class)) {
                    WeakAction action = weakActionAndToken.getAction();
                    if (action != null && obj == action.getTarget() && (bindingAction == null || bindingAction == action.getBindingAction())) {
                        if (obj2 == null || obj2.equals(weakActionAndToken.getToken())) {
                            weakActionAndToken.getAction().markForDeletion();
                        }
                    }
                }
            }
        }
    }

    private static boolean classImplements(Type type, Type type2) {
        if (type2 == null || type == null) {
            return false;
        }
        for (Class<?> cls : ((Class) type).getInterfaces()) {
            if (cls == type2) {
                return true;
            }
        }
        return false;
    }

    private static void cleanupList(HashMap<Type, List<WeakActionAndToken>> hashMap) {
        if (hashMap != null) {
            for (Map.Entry<Type, List<WeakActionAndToken>> entry : hashMap.entrySet()) {
                List<WeakActionAndToken> list = hashMap.get(entry);
                if (list != null) {
                    for (WeakActionAndToken weakActionAndToken : list) {
                        if (weakActionAndToken.getAction() == null || !weakActionAndToken.getAction().isLive()) {
                            list.remove(weakActionAndToken);
                        }
                    }
                    if (list.size() == 0) {
                        hashMap.remove(entry);
                    }
                }
            }
        }
    }

    private void sendToTargetOrType(Type type, Object obj) {
        if (this.recipientsOfSubclassesAction != null) {
            ArrayList<Type> arrayList = new ArrayList();
            arrayList.addAll(this.recipientsOfSubclassesAction.keySet());
            for (Type type2 : arrayList) {
                List<WeakActionAndToken> list = null;
                if (NotMsgType.class == type2 || ((Class) type2).isAssignableFrom(NotMsgType.class) || classImplements(NotMsgType.class, type2)) {
                    list = this.recipientsOfSubclassesAction.get(type2);
                }
                sendToList(list, type, obj);
            }
        }
        HashMap<Type, List<WeakActionAndToken>> hashMap = this.recipientsStrictAction;
        if (hashMap != null && hashMap.containsKey(NotMsgType.class)) {
            sendToList(this.recipientsStrictAction.get(NotMsgType.class), type, obj);
        }
        cleanup();
    }

    private static void sendToList(Collection<WeakActionAndToken> collection, Type type, Object obj) {
        if (collection != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(collection);
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                WeakActionAndToken weakActionAndToken = (WeakActionAndToken) it.next();
                WeakAction action = weakActionAndToken.getAction();
                if (action != null && weakActionAndToken.getAction().isLive() && weakActionAndToken.getAction().getTarget() != null && (type == null || weakActionAndToken.getAction().getTarget().getClass() == type || classImplements(weakActionAndToken.getAction().getTarget().getClass(), type))) {
                    if ((weakActionAndToken.getToken() == null && obj == null) || (weakActionAndToken.getToken() != null && weakActionAndToken.getToken().equals(obj))) {
                        action.execute();
                    }
                }
            }
        }
    }

    private <T> void sendToTargetOrType(T t, Type type, Object obj) {
        Class<?> cls = t.getClass();
        if (this.recipientsOfSubclassesAction != null) {
            ArrayList<Type> arrayList = new ArrayList();
            arrayList.addAll(this.recipientsOfSubclassesAction.keySet());
            for (Type type2 : arrayList) {
                List<WeakActionAndToken> list = null;
                if (cls == type2 || ((Class) type2).isAssignableFrom(cls) || classImplements(cls, type2)) {
                    list = this.recipientsOfSubclassesAction.get(type2);
                }
                sendToList(t, list, type, obj);
            }
        }
        HashMap<Type, List<WeakActionAndToken>> hashMap = this.recipientsStrictAction;
        if (hashMap != null && hashMap.containsKey(cls)) {
            sendToList(t, this.recipientsStrictAction.get(cls), type, obj);
        }
        cleanup();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: me.goldze.mvvmhabit.bus.Messenger$WeakActionAndToken */
    /* loaded from: classes.dex */
    public class WeakActionAndToken {
        private WeakAction action;
        private Object token;

        public WeakActionAndToken(WeakAction weakAction, Object obj) {
            this.action = weakAction;
            this.token = obj;
        }

        public WeakAction getAction() {
            return this.action;
        }

        public void setAction(WeakAction weakAction) {
            this.action = weakAction;
        }

        public Object getToken() {
            return this.token;
        }

        public void setToken(Object obj) {
            this.token = obj;
        }
    }
}
