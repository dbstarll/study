package io.github.dbstarll.study.service.impl;

import com.mongodb.Function;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.collection.CollectionNameGenerator;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.study.entity.ExerciseBook;
import io.github.dbstarll.study.entity.Principal;
import io.github.dbstarll.study.entity.enums.Module;
import io.github.dbstarll.study.service.PrincipalService;
import io.github.dbstarll.study.service.attach.PrincipalServiceAttach;
import io.github.dbstarll.study.utils.PageQuery;
import io.github.dbstarll.utils.lang.wrapper.EntryWrapper;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import static org.apache.commons.lang3.Validate.notNull;

public final class PrincipalServiceImplemental extends StudyImplementals<Principal, PrincipalService>
        implements PrincipalServiceAttach {
  private static DecoderContext decoderContext = DecoderContext.builder().build();

  private Codec<Principal> entityCodec;
  private Codec<ExerciseBook> exerciseBookCodec;
  private CollectionNameGenerator collectionNameGenerator;

  public PrincipalServiceImplemental(PrincipalService service, Collection<Principal> collection) {
    super(service, collection);
  }

  /**
   * set mongoDatabase.
   *
   * @param mongoDatabase mongoDatabase
   */
  public void setMongoDatabase(MongoDatabase mongoDatabase) {
    final CodecRegistry registry = mongoDatabase.getCodecRegistry();
    this.entityCodec = registry.get(entityClass);
    this.exerciseBookCodec = registry.get(ExerciseBook.class);
  }

  public void setCollectionNameGenerator(CollectionNameGenerator collectionNameGenerator) {
    this.collectionNameGenerator = collectionNameGenerator;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    notNull(entityCodec, "entityCodec is null");
    notNull(exerciseBookCodec, "exerciseBookCodec is null");
    notNull(collectionNameGenerator, "collectionNameGenerator is null");
  }

  @Override
  public MongoIterable<Entry<Principal, ExerciseBook>> findWithEntity(final Bson filter, final PageQuery query) {
    final List<Bson> pipeline = new LinkedList<>();
    if (filter != null) {
      pipeline.add(Aggregates.match(filter));
    }
    pipeline.add(exerciseLookup());
    query.apply(pipeline);

    return getCollection().aggregate(pipeline, BsonDocument.class)
            .map(new Function<BsonDocument, Entry<Principal, ExerciseBook>>() {
              @Override
              public Entry<Principal, ExerciseBook> apply(BsonDocument t) {
                final BsonArray books = t.getArray("exercises");
                final Principal principal = entityCodec.decode(t.asBsonReader(), decoderContext);
                final ExerciseBook book = books.size() > 0
                        ? exerciseBookCodec.decode(((BsonDocument) books.get(0)).asBsonReader(), decoderContext)
                        : null;
                return EntryWrapper.wrap(principal, book);
              }
            });
  }

  private Bson exerciseLookup() {
    return Aggregates.lookup(collectionNameGenerator.generateCollectionName(ExerciseBook.class),
            Sourceable.FIELD_NAME_SOURCES + '.' + Module.ENGLISH, Entity.FIELD_NAME_ID, "exercises");
  }
}
