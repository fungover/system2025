package org.fungover.system2025.springboot;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MapStore {
    public record Marker(String id, double lat, double lng, String text, String layer){}

    private final Map<String, List<Marker>> maps = new ConcurrentHashMap<>();

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
}
