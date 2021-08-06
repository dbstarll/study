package io.github.dbstarll.study.entity.join;

import io.github.dbstarll.dubai.model.entity.JoinBase;
import org.bson.types.ObjectId;

public interface VoiceBase extends JoinBase {
  String FIELD_NAME_VOICE_ID = "voiceId";

  ObjectId getVoiceId();

  void setVoiceId(ObjectId voiceId);
}
