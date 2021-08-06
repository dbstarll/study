package io.github.dbstarll.study.service;

import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.attach.ContentAttach;
import io.github.dbstarll.dubai.model.service.attach.SourceAttach;
import io.github.dbstarll.study.entity.Voice;

@EntityService
public interface VoiceService extends StudyServices<Voice>, SourceAttach<Voice>, ContentAttach<Voice> {

}
