package io.github.dbstarll.study.boot.controller.english;

import io.github.dbstarll.study.boot.controller.EntityNotFoundException;
import io.github.dbstarll.study.entity.Voice;
import io.github.dbstarll.study.service.VoiceService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "/english/voice")
class NewVoiceController {
    @Autowired
    private VoiceService voiceService;
    @Autowired
    private CacheControl cacheControl;

    @GetMapping(path = "/{voiceId}", produces = "audio/mpeg")
    ResponseEntity<byte[]> show(@PathVariable final ObjectId voiceId,
                                @RequestHeader(name = HttpHeaders.RANGE, required = false) final List<HttpRange> ranges) {
        final Voice voice = voiceService.findById(voiceId);
        if (voice == null) {
            throw new EntityNotFoundException(Voice.class, voiceId);
        }

        if (ranges == null) {
            return ResponseEntity.ok().cacheControl(cacheControl).body(voice.getContent());
        } else if (ranges.size() != 1) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).body(null);
        } else {
            final HttpRange range = ranges.get(0);
            final long length = voice.getContent().length;
            final int rangeStart = (int) range.getRangeStart(length);
            final int rangeEnd = (int) range.getRangeEnd(length) + 1;
            return ResponseEntity.status(rangeEnd < length ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes").header(HttpHeaders.CONTENT_RANGE, "bytes " + range + "/" + length)
                    .cacheControl(cacheControl).body(Arrays.copyOfRange(voice.getContent(), rangeStart, rangeEnd));
        }
    }
}
