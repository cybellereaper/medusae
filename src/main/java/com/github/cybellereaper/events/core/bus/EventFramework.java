package com.github.cybellereaper.events.core.bus;

import com.github.cybellereaper.events.core.exception.EventExceptionHandler;
import com.github.cybellereaper.events.core.filter.EventFilter;
import com.github.cybellereaper.events.core.intent.IntentDiagnostic;
import com.github.cybellereaper.events.core.model.EventContext;
import com.github.cybellereaper.events.core.model.EventListenerDefinition;
import com.github.cybellereaper.gateway.GatewayIntent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

public final class EventFramework {
    private final EventListenerParser parser = new EventListenerParser();
    private final Map<Class<?>, List<EventListenerDefinition>> listenersByType = new ConcurrentHashMap<>();
    private final Map<String, EventFilter> filters = new ConcurrentHashMap<>();
    private final Set<GatewayIntent> enabledIntents;
    private final ExecutorService asyncExecutor;
    private EventExceptionHandler exceptionHandler = EventExceptionHandler.rethrowing();

    public EventFramework(Set<GatewayIntent> enabledIntents) {
        this.enabledIntents = Set.copyOf(enabledIntents);
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "jellycord-events-async");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void registerListeners(Object... listeners) {
        for (Object listener : listeners) {
            for (EventListenerDefinition definition : parser.parse(listener)) {
                listenersByType.computeIfAbsent(definition.eventType(), ignored -> new ArrayList<>()).add(definition);
            }
        }
        listenersByType.values().forEach(list -> list.sort(Comparator.comparingInt(EventListenerDefinition::order).reversed()));
    }

    public void registerFilter(String id, EventFilter filter) {
        filters.put(id, filter);
    }

    public List<IntentDiagnostic> intentDiagnostics() {
        List<IntentDiagnostic> diagnostics = new ArrayList<>();
        listenersByType.values().forEach(definitions -> definitions.forEach(definition -> {
            Set<GatewayIntent> missing = definition.requiredIntents().stream().filter(intent -> !enabledIntents.contains(intent)).collect(java.util.stream.Collectors.toSet());
            if (!missing.isEmpty()) {
                diagnostics.add(new IntentDiagnostic(definition.method().toGenericString(), missing));
            }
        }));
        return List.copyOf(diagnostics);
    }

    public void setExceptionHandler(EventExceptionHandler exceptionHandler) {
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    public void dispatch(Object event) {
        dispatch(new EventContext(event, event));
    }

    public void dispatch(EventContext context) {
        List<EventListenerDefinition> listeners = listenersByType.getOrDefault(context.event().getClass(), List.of());
        List<EventListenerDefinition> executed = new ArrayList<>();
        for (EventListenerDefinition listener : listeners) {
            if (!listener.requiredIntents().stream().allMatch(enabledIntents::contains)) {
                continue;
            }
            if (!filtersPass(listener, context)) {
                continue;
            }
            Runnable invocation = () -> invokeListener(listener, context);
            if (listener.async()) {
                asyncExecutor.submit(invocation);
            } else {
                invocation.run();
            }
            if (listener.once()) {
                executed.add(listener);
            }
        }
        if (!executed.isEmpty()) {
            listeners.removeAll(executed);
        }
    }

    private boolean filtersPass(EventListenerDefinition definition, EventContext context) {
        for (String filterId : definition.filters()) {
            EventFilter filter = filters.get(filterId);
            if (filter == null) {
                throw new IllegalStateException("Unknown event filter: " + filterId);
            }
            if (!filter.test(context)) {
                return false;
            }
        }
        return true;
    }

    private void invokeListener(EventListenerDefinition definition, EventContext context) {
        try {
            Object[] args = resolveArguments(definition, context);
            definition.method().invoke(definition.instance(), args);
        } catch (Throwable throwable) {
            exceptionHandler.onException(context, unwrap(throwable));
        }
    }

    private static Object[] resolveArguments(EventListenerDefinition definition, EventContext context) {
        Class<?>[] parameterTypes = definition.method().getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isInstance(context.event())) {
                args[i] = context.event();
            } else if (parameterTypes[i] == EventContext.class) {
                args[i] = context;
            } else if (parameterTypes[i] == Object.class) {
                args[i] = context.rawPayload();
            } else {
                throw new IllegalArgumentException("Unsupported event listener parameter: " + parameterTypes[i].getName());
            }
        }
        return args;
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocationTargetException && invocationTargetException.getCause() != null) {
            return invocationTargetException.getCause();
        }
        return throwable;
    }
}
