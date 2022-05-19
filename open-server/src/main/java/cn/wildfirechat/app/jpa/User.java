package cn.wildfirechat.app.jpa;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_user", uniqueConstraints = {
		@UniqueConstraint(columnNames={"mobile"}),
		@UniqueConstraint(columnNames={"email"})
})
public class User implements Serializable {
	@Id
	@Column(length = 64)
	private String account;

	@Column(length = 64)
	private String mobile;

	@Column(length = 64)
	private String email;

	@Column(length = 16)
	private String role;

	@Column(length = 128)
	private String salt;

	@Column(length = 128)
	private String passwordMd5;

	public String getAccount() {
		return account;
	}


	public void setAccount(String account) {
		this.account = account;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getPasswordMd5() {
		return passwordMd5;
	}

	public void setPasswordMd5(String passwordMd5) {
		this.passwordMd5 = passwordMd5;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
