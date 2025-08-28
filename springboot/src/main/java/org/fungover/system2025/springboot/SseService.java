package org.fungover.system2025.springboot;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class SseService implements DisposableBean {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public SseService() {
        // Send a heartbeat every 20 seconds to keep connections alive
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe(String mapId){
        var emitter = new SseEmitter(0L);
        // Use CopyOnWriteArrayList for thread safety
        emitters.computeIfAbsent(mapId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(mapId, emitter));
        emitter.onTimeout(() -> remove(mapId, emitter)); // Should not happen with 0L, but good practice
        return emitter;
    }

    public void send(String mapId, Object event){
        var list = emitters.get(mapId);
        if(list==null) return;
        // Iterate over a thread-safe list
        for(var emitter : list){
            try{
                emitter.send(event);
            }catch(IOException | IllegalStateException e){
                // onCompletion will be called automatically, which triggers remove()
            }
        }
    }

    private void sendHeartbeat() {
        for (List<SseEmitter> list : emitters.values()) {
            for (SseEmitter emitter : list) { // CopyOnWriteArrayList is safe to iterate
                try {
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                } catch (IOException | IllegalStateException e) {
                    // onCompletion will be called automatically, which triggers remove()
                }
            }
        }
    }

    private void remove(String mapId, SseEmitter emitter){
        var list = emitters.get(mapId);
        if(list != null){
            list.remove(emitter);
            // Clean up the map if the list is empty
            if (list.isEmpty()) {
                emitters.remove(mapId);
            }
        }
    }

    @Override
    public void destroy() {
        heartbeatExecutor.shutdown();
    }
}
