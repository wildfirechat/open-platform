package cn.wildfirechat.app;


import cn.wildfirechat.app.pojo.PojoApplicationEntity;
import cn.wildfirechat.pojos.InputCreateDevice;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface Service {
    RestResult login(HttpServletResponse response, String account, String password);
    RestResult clientLogin(HttpServletResponse response, String appId, int appType, String authcode);
    RestResult updatePassword(String oldPassword, String newPassword);
    RestResult getAccount();
    RestResult createApplication(PojoApplicationEntity pojoApplicationEntity) throws Exception;
    RestResult updateApplication(PojoApplicationEntity pojoApplicationEntity) throws Exception;
    RestResult deleteApplication(String targetId) throws Exception;
    RestResult uploadMedia(MultipartFile file) throws Exception;
    RestResult getApplication(String targetId);
    RestResult listApplication(int type);
    RestResult listGlobalApplication();
    RestResult favApplication(String targetId);
    RestResult unfavApplication(String targetId);
    RestResult getFavApplications();
}
