package koa.repository;

import java.util.Collection;

public interface Repository<T> {

    void save();
    T findById(long id);
    Collection<T> getAll();
    void insert(T element);
    void delete(long id);

}
