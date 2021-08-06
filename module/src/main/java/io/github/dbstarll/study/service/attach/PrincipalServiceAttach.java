package io.github.dbstarll.study.service.attach;

import com.mongodb.client.MongoIterable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.service.impl.PrincipalServiceImplemental;
import io.github.dbstarll.study.utils.PageQuery;
import org.bson.conversions.Bson;

import java.util.Map.Entry;

@Implementation(PrincipalServiceImplemental.class)
public interface PrincipalServiceAttach extends StudyAttachs {
  MongoIterable<Entry<Principal, ExerciseBook>> findWithEntity(Bson filter, PageQuery query);
}
