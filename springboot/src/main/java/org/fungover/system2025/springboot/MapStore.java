package org.fungover.system2025.springboot;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MapStore {
    public record Marker(String id, double lat, double lng, String text, String layer){}
    public record StoredEvent(String id, Object data) {}

    private final Map<String, List<Marker>> maps = new ConcurrentHashMap<>();
    private final Map<String, List<StoredEvent>> eventLog = new ConcurrentHashMap<>();

    public List<Marker> getMarkers(String mapId){
        return maps.computeIfAbsent(mapId, k -> Collections.synchronizedList(new ArrayList<>()));
    }

    public Marker addMarker(String mapId, double lat, double lng, String text, String layer){
        var marker = new Marker(UUID.randomUUID().toString(), lat, lng, text, layer);
        getMarkers(mapId).add(marker);
        return marker;
    }

    public boolean deleteMarker(String mapId, String markerId){
        return getMarkers(mapId).removeIf(m -> m.id().equals(markerId));
    }

    public StoredEvent saveEvent(String mapId, Object data) {
        String eventId = String.valueOf(System.currentTimeMillis());
        StoredEvent event = new StoredEvent(eventId, data);
        eventLog.computeIfAbsent(mapId, k -> Collections.synchronizedList(new ArrayList<>())).add(event);
        return event;
    }

    public List<StoredEvent> getEventsSince(String mapId, String lastEventId) {
        List<StoredEvent> events = eventLog.get(mapId);
        if (events == null) return Collections.emptyList();

        List<StoredEvent> result = new ArrayList<>();
        boolean found = false;
        synchronized (events) {
            for (StoredEvent event : events) {
                if (found) {
                    result.add(event);
                } else if (event.id().equals(lastEventId)) {
                    found = true;
                }
            }
        }
        return result;
    }
}
