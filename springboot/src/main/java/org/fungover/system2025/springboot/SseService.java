package org.fungover.system2025.springboot;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseService {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String mapId){
        var emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(mapId, k -> new ArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(mapId, emitter));
        emitter.onTimeout(() -> remove(mapId, emitter));
        return emitter;
    }

    public void send(String mapId, Object event){
        var list = emitters.get(mapId);
        if(list==null) return;
        List<SseEmitter> dead = new ArrayList<>();
        for(var em : list){
            try{
                em.send(event);
            }catch(IOException e){
                dead.add(em);
            }
        }
        if(!dead.isEmpty()) list.removeAll(dead);
    }

    private void remove(String mapId, SseEmitter emitter){
        var list = emitters.get(mapId);
        if(list!=null){ list.remove(emitter); }
    }
}
