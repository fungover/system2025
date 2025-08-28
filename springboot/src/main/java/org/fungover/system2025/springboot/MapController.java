package org.fungover.system2025.springboot;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MapController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/{mapId}")
    public String map(@PathVariable String mapId, Model model) {
        model.addAttribute("mapId", mapId);
        return "map";
    }

    @GetMapping("/api/info")
    @ResponseBody
    public String info() {
        return "Markers are stored in memory and are not persisted.";
    }
}
