package io.github.dbstarll.study.boot.controller;

import io.github.dbstarll.study.entity.Exercise.ExerciseKey;
import org.bson.types.ObjectId;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/exercise", produces = MediaType.APPLICATION_JSON_VALUE)
class ExerciseController {
    @GetMapping(path = "/{key}", produces = MediaType.TEXT_HTML_VALUE)
    String exercisePage(@PathVariable final ExerciseKey key, final Model model) {
        model.addAttribute("bookId", new ObjectId("5c1349fac0ad810254687ca1"));
        return "exercise/" + key.name();
    }

    @GetMapping(path = "/{key}/{bookId}", produces = MediaType.TEXT_HTML_VALUE)
    String exercisePage(@PathVariable("key") final ExerciseKey key, @PathVariable("bookId") final ObjectId bookId,
                        final Model model) {
        model.addAttribute("bookId", bookId);
        return "exercise/" + key.name();
    }
}
