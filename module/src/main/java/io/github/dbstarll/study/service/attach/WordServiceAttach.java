package io.github.dbstarll.study.service.attach;

import com.mongodb.client.AggregateIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.study.entity.Word;
import io.github.dbstarll.study.service.impl.WordServiceImplemental;
import io.github.dbstarll.study.service.impl.WordServiceImplemental.WordWithJoin;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(WordServiceImplemental.class)
public interface WordServiceAttach extends StudyAttachs, ServiceSaver<Word> {
  Bson filterByWord(String word, boolean matchExchange, boolean fuzzyMatching);

  AggregateIterable<Word> sample(Bson filter, int num);

  Iterable<WordWithJoin> findWithJoin(Bson filter, String joinTable, String joinField, ObjectId joinId,
                                      PageQuery query);
}
