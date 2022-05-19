package cn.wildfirechat.app;


import cn.wildfirechat.app.jpa.*;
import cn.wildfirechat.app.pojo.*;
import cn.wildfirechat.app.shiro.AuthCodeToken;
import cn.wildfirechat.app.tools.RateLimiter;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.*;
import cn.wildfirechat.sdk.model.IMResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static cn.wildfirechat.app.RestResult.RestCode.*;
import static cn.wildfirechat.proto.ProtoConstants.ChannelState.*;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceImpl.class);

    @Value("${im.admin_url}")
    private String mAdminUrl;

    @Value("${im.admin_secret}")
    private String mAdminSecret;

    @Autowired
    private ApplicationEntityRepository applicationEntityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    private RateLimiter rateLimiter;

    @PostConstruct
    private void init() {
        AdminConfig.initAdmin(mAdminUrl, mAdminSecret);
        rateLimiter = new RateLimiter(60, 200);
    }

    public RestResult login(HttpServletResponse httpResponse, String account, String password) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(account, password);

        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return RestResult.error(ERROR_CODE_ACCOUNT_NOT_EXIST);
        } catch (IncorrectCredentialsException ice) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (LockedAccountException lae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (ExcessiveAttemptsException eae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (AuthenticationException ae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        }

        if (subject.isAuthenticated()) {
            long timeout = subject.getSession().getTimeout();
            LOG.info("Login success " + timeout);
        } else {
            token.clear();
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        }

        Object sessionId = subject.getSession().getId();
        httpResponse.setHeader("authToken", sessionId.toString());

        return RestResult.ok(null);
    }

    @Override
    public RestResult clientLogin(HttpServletResponse httpResponse, String appId, int appType, String authcode) {
        Subject subject = SecurityUtils.getSubject();
        AuthCodeToken token = new AuthCodeToken(appId, appType, authcode);

        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return RestResult.error(ERROR_CODE_ACCOUNT_NOT_EXIST);
        } catch (IncorrectCredentialsException ice) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (LockedAccountException lae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (ExcessiveAttemptsException eae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        } catch (AuthenticationException ae) {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        }

        if (subject.isAuthenticated()) {
            long timeout = subject.getSession().getTimeout();
            LOG.info("Login success " + timeout);
        } else {
            return RestResult.error(RestResult.RestCode.ERROR_CODE_PASSWORD_INCORRECT);
        }

        Object sessionId = subject.getSession().getId();
        httpResponse.setHeader("authToken", sessionId.toString());

        return RestResult.ok(null);
    }

    @Override
    public RestResult updatePassword(String oldPassword, String newPassword) {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) {
            return RestResult.error(ERROR_NOT_LOGIN);
        }
        String account = (String)subject.getPrincipal();
        Optional<User> optionalUser = userRepository.findByAccount(account);
        if(!optionalUser.isPresent()) {
            return RestResult.error(ERROR_CODE_ACCOUNT_NOT_EXIST);
        }

        User user = optionalUser.get();
        String md5 = new Base64().encodeToString(DigestUtils.getDigest("MD5").digest((oldPassword + user.getSalt()).getBytes(StandardCharsets.UTF_8)));
        if(!md5.equals(user.getPasswordMd5())) {
            return RestResult.error(ERROR_CODE_PASSWORD_INCORRECT);
        }
        
        String newMd5 = new Base64().encodeToString(DigestUtils.getDigest("MD5").digest((newPassword + user.getSalt()).getBytes(StandardCharsets.UTF_8)));
        user.setPasswordMd5(newMd5);
        userRepository.save(user);

        return RestResult.ok(null);
    }

    public String getUserId() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            return (String) subject.getPrincipal();
        }
        return null;
    }

    @Override
    public RestResult getAccount() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            return RestResult.ok(subject.getPrincipal());
        }
        return RestResult.error(ERROR_NOT_EXIST);
    }

    @Override
    public RestResult createApplication(PojoApplicationEntity pojoApplicationEntity) throws Exception {
        if(StringUtils.isNullOrEmpty(pojoApplicationEntity.targetId)) {
            pojoApplicationEntity.targetId = UUID.randomUUID().toString();
        }
        if(applicationEntityRepository.findByTargetId(pojoApplicationEntity.targetId).isPresent()) {
            return RestResult.error(ERROR_CODE_APPLICATION_ALREADY_EXIST);
        }
        //save application entry;
        if(StringUtils.isNullOrEmpty(pojoApplicationEntity.secret)) {
            pojoApplicationEntity.secret = UUID.randomUUID().toString();
        }
        pojoApplicationEntity.createDt = System.currentTimeMillis();
        pojoApplicationEntity.updateDt = pojoApplicationEntity.createDt;

        ApplicationEntity entity = convertApplicationPojo(pojoApplicationEntity);
        applicationEntityRepository.save(entity);

        //create robot
        createRobotByApplicationEntity(entity);
        createChannelByApplicationEntity(entity);

        return RestResult.ok(pojoApplicationEntity);
    }

    private void createRobotByApplicationEntity(ApplicationEntity entity) throws Exception {
        InputCreateRobot inputCreateRobot = new InputCreateRobot();
        inputCreateRobot.setUserId(entity.getTargetId());
        inputCreateRobot.setName(entity.getTargetId());
        inputCreateRobot.setDisplayName(entity.getName());
        inputCreateRobot.setPortrait(entity.getPortraitUrl());
        inputCreateRobot.setOwner("admin");
        inputCreateRobot.setSecret(entity.getSecret());
        inputCreateRobot.setCallback(entity.getServerUrl());
        IMResult<OutputCreateRobot> outputCreateRobotIMResult = UserAdmin.createRobot(inputCreateRobot);
        if(outputCreateRobotIMResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
            throw new Exception("create robot failure");
        }
    }

    private void createChannelByApplicationEntity(ApplicationEntity entity) throws Exception {
        InputCreateChannel inputCreateChannel = new InputCreateChannel();
        inputCreateChannel.setTargetId(entity.getTargetId());
        inputCreateChannel.setOwner(entity.getTargetId());
        inputCreateChannel.setName(entity.getName());
        inputCreateChannel.setPortrait(entity.getPortraitUrl());
        inputCreateChannel.setCallback(entity.getServerUrl());
        inputCreateChannel.setAuto(1);
        inputCreateChannel.setSecret(entity.getSecret());
        inputCreateChannel.setDesc(entity.getDescription());
        inputCreateChannel.setState(Channel_State_Mask_Active_Subscribe | Channel_State_Mask_FullInfo | Channel_State_Mask_Message_Unsubscribed | Channel_State_Mask_Unsubscribed_User_Access);
        IMResult<OutputCreateChannel> outputCreateChannelIMResult = GeneralAdmin.createChannel(inputCreateChannel);
        if(outputCreateChannelIMResult.getErrorCode() != ErrorCode.ERROR_CODE_SUCCESS) {
            throw new Exception("create channel failure");
        }
    }

    @Override
    public RestResult updateApplication(PojoApplicationEntity pojoApplicationEntity) throws Exception {
        if(StringUtils.isNullOrEmpty(pojoApplicationEntity.targetId)) {
            return RestResult.error(ERROR_INVALID_PARAMETER);
        }

        Optional<ApplicationEntity> optionalApplicationEntity = applicationEntityRepository.findByTargetId(pojoApplicationEntity.targetId);
        if(!optionalApplicationEntity.isPresent()) {
            return RestResult.error(ERROR_NOT_EXIST);
        }

        //save application entry;
        if(StringUtils.isNullOrEmpty(pojoApplicationEntity.secret)) {
            pojoApplicationEntity.secret = UUID.randomUUID().toString();
        }
        pojoApplicationEntity.updateDt = System.currentTimeMillis();

        if(pojoApplicationEntity.createDt == 0) {
            pojoApplicationEntity.createDt = optionalApplicationEntity.get().getCreateDt();
        }


        ApplicationEntity entity = convertApplicationPojo(pojoApplicationEntity);
        applicationEntityRepository.save(entity);

        createRobotByApplicationEntity(entity);
        createChannelByApplicationEntity(entity);

        return RestResult.ok(pojoApplicationEntity);
    }

    @Override
    public RestResult deleteApplication(String targetId) throws Exception {
        Optional<ApplicationEntity> optionalApplicationEntity = applicationEntityRepository.findByTargetId(targetId);
        if(!optionalApplicationEntity.isPresent()) {
            return RestResult.error(ERROR_NOT_EXIST);
        }
        applicationEntityRepository.delete(optionalApplicationEntity.get());
        GeneralAdmin.destroyChannel(optionalApplicationEntity.get().getTargetId());
        UserAdmin.destroyRobot(optionalApplicationEntity.get().getTargetId());
        return RestResult.ok(null);
    }

    @Override
    public RestResult getApplication(String targetId) {
        Optional<ApplicationEntity> optionalApplicationEntity = applicationEntityRepository.findByTargetId(targetId);
        if(!optionalApplicationEntity.isPresent()) {
            return RestResult.error(ERROR_NOT_EXIST);
        }
        ApplicationEntity entity = optionalApplicationEntity.get();

        Subject subject = SecurityUtils.getSubject();
        boolean fullInfo = false;
        if(subject.isAuthenticated() && subject.isPermitted("user:admin")) {
            fullInfo = true;
        }

        PojoApplicationEntity pojoApplicationEntity = convertApplicationEntity(entity, fullInfo);

        return RestResult.ok(pojoApplicationEntity);
    }

    @Override
    public RestResult listApplication() {
        Subject subject = SecurityUtils.getSubject();
        boolean fullInfo = subject.isAuthenticated() && subject.isPermitted("user:admin");

        List<PojoApplicationEntity> list = new ArrayList<>();
        applicationEntityRepository.findAll().forEach(entity -> list.add(convertApplicationEntity(entity, fullInfo)));

        return RestResult.ok(list);
    }

    @Override
    public RestResult listGlobalApplication() {
        Subject subject = SecurityUtils.getSubject();
        boolean fullInfo = subject.isAuthenticated() && subject.isPermitted("user:admin");

        List<PojoApplicationEntity> list = new ArrayList<>();
        applicationEntityRepository.findAllGlobalEntity().forEach(entity -> list.add(convertApplicationEntity(entity, fullInfo)));
        return RestResult.ok(list);
    }

    @Override
    public RestResult favApplication(String targetId) {
        UserApplication userApplication = new UserApplication();
        userApplication.userId = getUserId();
        userApplication.applicationId = targetId;
        userApplicationRepository.save(userApplication);
        return RestResult.ok(null);
    }

    @Override
    public RestResult unfavApplication(String targetId) {
        UserApplication userApplication = new UserApplication();
        userApplication.userId = getUserId();
        userApplication.applicationId = targetId;
        userApplicationRepository.delete(userApplication);
        return RestResult.ok(null);
    }

    @Override
    public RestResult getFavApplications() {
        List<ApplicationEntityDTO> applicationEntityDTOS = userApplicationRepository.getUserApplications(getUserId());
        List<ApplicationEntity> entities = new ArrayList<>();
        applicationEntityDTOS.forEach(dto -> {
            ApplicationEntity entity = new ApplicationEntity();
            entity.setTargetId(dto.getTarget_id());
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setPortraitUrl(dto.getPortrait_url());
            entity.setMobileUrl(dto.getMobile_url());
            entity.setDesktopUrl(dto.getDesktop_url());
            entity.setGlobal(dto.isGlobal());
            entity.setUpdateDt(dto.getUpdate_dt());
            entity.setCreateDt(dto.getCreate_dt());
            entities.add(entity);
        });
        return RestResult.ok(entities);
    }

    private ApplicationEntity convertApplicationPojo(PojoApplicationEntity pojoApplicationEntity) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setTargetId(pojoApplicationEntity.targetId);
        entity.setSecret(pojoApplicationEntity.secret);
        entity.setName(pojoApplicationEntity.name);
        entity.setDescription(pojoApplicationEntity.description);
        entity.setPortraitUrl(pojoApplicationEntity.portraitUrl);
        entity.setMobileUrl(pojoApplicationEntity.mobileUrl);
        entity.setDesktopUrl(pojoApplicationEntity.desktopUrl);
        entity.setServerUrl(pojoApplicationEntity.serverUrl);
        entity.setGlobal(pojoApplicationEntity.global);
        entity.setCreateDt(pojoApplicationEntity.createDt);
        entity.setUpdateDt(pojoApplicationEntity.updateDt);
        return entity;
    }

    private PojoApplicationEntity convertApplicationEntity(ApplicationEntity entity, boolean fullInfo) {
        PojoApplicationEntity pojoApplicationEntity = new PojoApplicationEntity();
        pojoApplicationEntity.targetId = entity.getTargetId();
        if(fullInfo) {
            pojoApplicationEntity.secret = entity.getSecret();
            pojoApplicationEntity.serverUrl = entity.getServerUrl();
        }
        pojoApplicationEntity.name = entity.getName();
        pojoApplicationEntity.description = entity.getDescription();
        pojoApplicationEntity.portraitUrl = entity.getPortraitUrl();
        pojoApplicationEntity.mobileUrl = entity.getMobileUrl();
        pojoApplicationEntity.desktopUrl = entity.getDesktopUrl();
        pojoApplicationEntity.global = entity.isGlobal();
        pojoApplicationEntity.updateDt = entity.getUpdateDt();
        pojoApplicationEntity.createDt = entity.getCreateDt();
        return pojoApplicationEntity;
    }

    private void sendTextMessage(String fromUser, String toUser, String text) {
        Conversation conversation = new Conversation();
        conversation.setTarget(toUser);
        conversation.setType(ProtoConstants.ConversationType.ConversationType_Private);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent(text);


        try {
            IMResult<SendMessageResult> resultSendMessage = MessageAdmin.sendMessage(fromUser, conversation, payload);
            if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
                LOG.info("send message success");
            } else {
                LOG.error("send message error {}", resultSendMessage != null ? resultSendMessage.getErrorCode().code : "unknown");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("send message error {}", e.getLocalizedMessage());
        }

    }
}
