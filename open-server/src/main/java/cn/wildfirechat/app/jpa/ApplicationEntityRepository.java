package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationEntityRepository extends CrudRepository<ApplicationEntity, String> {
    Optional<ApplicationEntity> findByTargetId(String targetId);

    @Query(value = "select * from t_application where global = true", nativeQuery = true)
    List<ApplicationEntity> findAllGlobalEntity();
}
