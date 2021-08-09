package io.github.dbstarll.study.boot.controller;

import com.baidu.aip.speech.AipSpeech;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

@RestController
@RequestMapping(path = "/voice")
class VoiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceController.class);

    private static final HashMap<String, Object> options = new HashMap<>(
            Collections.singletonMap("dev_pid", (Object) 1737));

    @Autowired
    private AipSpeech aipSpeech;

    @PostMapping(path = "/asr", consumes = "audio/wav", produces = MediaType.APPLICATION_JSON_VALUE)
    Object asrWav(@RequestBody final byte[] wav) throws UnsupportedAudioFileException, IOException {
        final long now = System.currentTimeMillis();
        try {
            return aipSpeech.asr(wav, "pcm", 16000, options).toString();
        } finally {
            LOGGER.debug("asrWav - asr: {}", System.currentTimeMillis() - now);
        }
    }

    @PostMapping(path = "/asr", consumes = "audio/mp3", produces = MediaType.APPLICATION_JSON_VALUE)
    Object asrMp3(@RequestBody final byte[] mp3) throws UnsupportedAudioFileException, IOException {
        final long now = System.currentTimeMillis();
        final byte[] pcm = mp3ToPcm(mp3);
        final long next = System.currentTimeMillis();
        try {
            return aipSpeech.asr(pcm, "pcm", 16000, options).toString();
        } finally {
            LOGGER.debug("asrMp3 - mp3ToPcm: {}, asr: {}", next - now, System.currentTimeMillis() - next);
        }
    }

    private byte[] mp3ToPcm(final byte[] mp3) throws UnsupportedAudioFileException, IOException {
        try (final AudioInputStream inMp3 = new MpegAudioFileReader().getAudioInputStream(new ByteArrayInputStream(mp3))) {
            final AudioFormat baseFormat = inMp3.getFormat();
            final AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                    baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

            try (final AudioInputStream inPcm = AudioSystem.getAudioInputStream(targetFormat, inMp3)) {
                File file = File.createTempFile("auido-", ".pcm");
                AudioSystem.write(inPcm, AudioFileFormat.Type.WAVE, file);
                return FileUtils.readFileToByteArray(file);
            }
        }
    }
}
