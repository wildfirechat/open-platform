package cn.wildfirechat.app.jpa;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_application")
public class ApplicationEntity implements Serializable {
	@Id
	@Column(length = 64, name = "target_id")
	private String targetId;

	@Column(length = 64)
	private String secret;

	@Column(length = 64)
	private String name;

	@Column(length = 1024)
	private String description;

	@Column(length = 1024)
	private String portraitUrl;

	@Column(length = 1024)
	private String mobileUrl;

	@Column(length = 1024)
	private String desktopUrl;

	@Column(length = 1024)
	private String serverUrl;

	@Column
	private boolean global;

	@Column(columnDefinition="tinyint(1) default 0")
	private boolean background;

	@Column
	private long updateDt;

	@Column
	private long createDt;

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPortraitUrl() {
		return portraitUrl;
	}

	public void setPortraitUrl(String portraitUrl) {
		this.portraitUrl = portraitUrl;
	}

	public String getMobileUrl() {
		return mobileUrl;
	}

	public void setMobileUrl(String mobileUrl) {
		this.mobileUrl = mobileUrl;
	}

	public String getDesktopUrl() {
		return desktopUrl;
	}

	public void setDesktopUrl(String desktopUrl) {
		this.desktopUrl = desktopUrl;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public boolean isBackground() {
		return background;
	}

	public void setBackground(boolean background) {
		this.background = background;
	}

	public long getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(long updateDt) {
		this.updateDt = updateDt;
	}

	public long getCreateDt() {
		return createDt;
	}

	public void setCreateDt(long createDt) {
		this.createDt = createDt;
	}
}
