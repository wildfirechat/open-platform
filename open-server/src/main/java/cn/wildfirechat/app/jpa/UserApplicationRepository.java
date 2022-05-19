package cn.wildfirechat.app.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserApplicationRepository extends CrudRepository<UserApplication, UserApplicationID> {
    @Query(value = "select c.* from t_user_application uc, t_application c where uc.user_id = ?1 and uc.application_id = c.target_id", nativeQuery = true)
    List<ApplicationEntityDTO> getUserApplications(String userId);
}
