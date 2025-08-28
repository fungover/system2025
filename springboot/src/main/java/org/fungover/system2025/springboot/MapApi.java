package org.fungover.system2025.springboot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MapApi {
    private final MapStore store;
    private final SseService sse;
    private static final String ID_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    public MapApi(MapStore store, SseService sse) {
        this.store = store;
        this.sse = sse;
    }

    @PostMapping("/new")
    public Map<String, String> createNewMap(){
        String id = generateId(5);
        // touch store so it exists
        store.getMarkers(id);
        return Map.of("id", id, "url", "/" + id);
    }

    @GetMapping("/{mapId}/events")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe(@PathVariable String mapId){
        return sse.subscribe(mapId);
    }

    @GetMapping("/{mapId}/markers")
    public List<MapStore.Marker> getMarkers(@PathVariable String mapId){
        return store.getMarkers(mapId);
    }

    public record AddMarkerRequest(double lat, double lng, String text, String layer){}

    @PostMapping("/{mapId}/markers")
    public MapStore.Marker addMarker(@PathVariable String mapId, @RequestBody AddMarkerRequest req){
        var m = store.addMarker(mapId, req.lat(), req.lng(), req.text(), req.layer());
        sse.send(mapId, Map.of("type","added","marker", m));
        return m;
    }

    @DeleteMapping("/{mapId}/markers/{markerId}")
    public ResponseEntity<Void> deleteMarker(@PathVariable String mapId, @PathVariable String markerId){
        boolean removed = store.deleteMarker(mapId, markerId);
        if(removed){
            sse.send(mapId, Map.of("type","deleted","markerId", markerId));
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private static String generateId(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i=0;i<len;i++){
            sb.append(ID_ALPHABET.charAt(RNG.nextInt(ID_ALPHABET.length())));
        }
        return sb.toString();
    }
}
