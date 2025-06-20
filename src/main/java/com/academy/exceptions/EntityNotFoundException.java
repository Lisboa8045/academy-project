package com.academy.exceptions;

public class EntityNotFoundException extends RuntimeException {
    private final Class<?> entityClass;
    private final long id;

    public EntityNotFoundException(Class<?> entityClass, long id) {
        super(entityClass.getSimpleName() + " with id " + id + " not found");
        this.entityClass = entityClass;
        this.id = id;
    }
    public EntityNotFoundException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
        this.entityClass = entityClass;
        this.id = -1;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public long getId() {
        return id;
    }
}
