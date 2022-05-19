package cn.wildfirechat.app.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

public interface ApplicationEntityDTO {
	public String getTarget_id();
	public String getSecret();
	public String getName();
	public String getDescription();
	public String getPortrait_url();
	public String getMobile_url();
	public String getDesktop_url();
	public String getServer_url();
	public boolean isGlobal();
	public long getUpdate_dt();
	public long getCreate_dt();
}
