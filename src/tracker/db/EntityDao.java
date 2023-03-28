package tracker.db;

import java.util.List;
import java.util.Optional;
import tracker.entities.Entity;

public interface EntityDao<T extends Entity> {

  void save(T entity);

  Optional<T> getById(int id);

  void update(T entity);

  void delete(T entity);

  List<Optional<T>> getAll();

}
