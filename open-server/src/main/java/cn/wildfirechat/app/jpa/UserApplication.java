package cn.wildfirechat.app.jpa;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(UserApplicationID.class)
@Table(name = "t_user_application")
public class UserApplication implements Serializable {
	@Id
	@Column(length = 64)
	public String userId;

	@Id
	@Column(length = 64)
	public String applicationId;
}
