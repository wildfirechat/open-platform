package cn.wildfirechat.app;

import cn.wildfirechat.app.pojo.ClientLoginRequest;
import cn.wildfirechat.app.pojo.LoginRequest;
import cn.wildfirechat.app.pojo.PojoApplicationEntity;
import cn.wildfirechat.app.pojo.UpdatePasswordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RequestMapping(value = "/api/")
@RestController
public class ApiController {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);
    @Autowired
    private Service mService;

    /*
    管理后台登陆
     */
    @PostMapping(value = "login", produces = "application/json;charset=UTF-8")
    public Object login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return mService.login(response, request.getAccount(), request.getPassword());
    }

    /*
    客户端登陆
     */
    @PostMapping(value = "user_login", produces = "application/json;charset=UTF-8")
    public Object userLogin(@RequestBody ClientLoginRequest request, HttpServletResponse response) {
        return mService.clientLogin(response, request.getAppId(), request.getAppType(), request.getAuthCode());
    }

    /*
    管理后台修改密码
     */
    @PostMapping(value = "update_pwd", produces = "application/json;charset=UTF-8")
    public Object updatePwd(@RequestBody UpdatePasswordRequest request) {
        return mService.updatePassword(request.oldPassword, request.newPassword);
    }

    /*
    获取当前用户ID，管理后台和客户端都可以使用
     */
    @GetMapping(value = "account", produces = "application/json;charset=UTF-8")
    public Object getAccount() {
        return mService.getAccount();
    }

    /*
    管理后台创建应用
     */
    @PostMapping(value = "application/create", produces = "application/json;charset=UTF-8")
    public Object createApplication(@RequestBody PojoApplicationEntity request) throws Exception {
        return mService.createApplication(request);
    }

    /*
    管理后台更新应用
     */
    @PostMapping(value = "application/update", produces = "application/json;charset=UTF-8")
    public Object updateApplication(@RequestBody PojoApplicationEntity request) throws Exception {
        return mService.updateApplication(request);
    }

    /*
    管理后台删除应用
     */
    @DeleteMapping(value = "application/del/{targetId}", produces = "application/json;charset=UTF-8")
    public Object delApplication(@PathVariable("targetId") String targetId) throws Exception {
        return mService.deleteApplication(targetId);
    }

    /*
    管理后台上传图片
     */
    @PostMapping(value = "application/media/upload")
    public Object uploadMedia(@RequestParam("file") MultipartFile file) throws Exception {
        return mService.uploadMedia(file);
    }

    /*
    获取应用信息，管理后台和客户端都可能调用
     */
    @GetMapping(value = "application/get/{targetId}", produces = "application/json;charset=UTF-8")
    public Object getApplication(@PathVariable("targetId") String targetId) {
        return mService.getApplication(targetId);
    }

    /*
    获取应用列表，管理后台和客户端都可以调用
     */
    @GetMapping(value = "application/list", produces = "application/json;charset=UTF-8")
    public Object listApplication(@RequestParam(value = "type", required = false, defaultValue = "0") int type) {
        return mService.listApplication(type);
    }

    /*
    获取全部应用列表，管理后台
    */
    @GetMapping(value = "application/list_all", produces = "application/json;charset=UTF-8")
    public Object listAllApplication() {
        return mService.listApplication(-1);
    }

    /*
    获取全局应用列表，管理后台和客户端都可以调用
    */
    @GetMapping(value = "application/list_global", produces = "application/json;charset=UTF-8")
    public Object listGlobalApplication() {
        return mService.listGlobalApplication();
    }

    /*
    客户端收藏应用
     */
    @PutMapping(value = "user/fav/{targetId}", produces = "application/json;charset=UTF-8")
    public Object favApplication(@PathVariable("targetId") String targetId) {
        return mService.favApplication(targetId);
    }

    /*
    客户端删除收藏应用
     */
    @DeleteMapping(value = "user/fav/{targetId}", produces = "application/json;charset=UTF-8")
    public Object unfavApplication(@PathVariable("targetId") String targetId) {
        return mService.unfavApplication(targetId);
    }

    /*
    客户端收藏应用
    */
    @GetMapping(value = "user/fav_list", produces = "application/json;charset=UTF-8")
    public Object getFavApplications() {
        return mService.getFavApplications();
    }
}
