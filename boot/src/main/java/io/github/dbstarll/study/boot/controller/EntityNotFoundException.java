package io.github.dbstarll.study.boot.controller;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.types.ObjectId;

public class EntityNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 6043150988839666132L;

    private final Class<? extends Entity> entityClass;
    private final ObjectId entityId;

    public EntityNotFoundException(Class<? extends Entity> entityClass, ObjectId entityId) {
        this(entityClass, entityId, message(entityClass, entityId), null);
    }

    public EntityNotFoundException(Class<? extends Entity> entityClass, ObjectId entityId, String message) {
        this(entityClass, entityId, message, null);
    }

    public EntityNotFoundException(Class<? extends Entity> entityClass, ObjectId entityId, Throwable cause) {
        this(entityClass, entityId, message(entityClass, entityId), cause);
    }

    /**
     * EntityNotFoundException.
     *
     * @param entityClass entityClass
     * @param entityId    entityId
     * @param message     message
     * @param cause       cause
     */
    public EntityNotFoundException(Class<? extends Entity> entityClass, ObjectId entityId, String message,
                                   Throwable cause) {
        super(message, cause);
        this.entityClass = entityClass;
        this.entityId = entityId;
    }

    private static String message(Class<? extends Entity> entityClass, ObjectId entityId) {
        return "Entity not found: " + entityClass + (entityId == null ? "" : ("@" + entityId));
    }

    public Class<? extends Entity> getEntityClass() {
        return entityClass;
    }

    public ObjectId getEntityId() {
        return entityId;
    }
}
