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
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.google.gson.Gson;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    @Value("${media.server.media_type}")
    private int ossType;

    @Value("${media.server_url}")
    private String ossUrl;

    @Value("${media.access_key}")
    private String ossAccessKey;

    @Value("${media.secret_key}")
    private String ossSecretKey;

    @Value("${media.bucket_name}")
    private String ossBucket;
    @Value("${media.bucket_domain}")
    private String ossBucketDomain;

    @Value("${local.media.temp_storage}")
    private String ossTempPath;

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
        inputCreateChannel.setState(Channel_State_Mask_Active_Subscribe | Channel_State_Mask_FullInfo | Channel_State_Mask_Message_Unsubscribed | Channel_State_Mask_Unsubscribed_User_Access | Channel_State_Mask_Private);
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
    public RestResult uploadMedia(MultipartFile file) throws Exception {
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        File localFile = new File(ossTempPath, fileName);

        try {
            file.transferTo(localFile);
        } catch (IOException e) {
            e.printStackTrace();
            return RestResult.error(ERROR_SERVER_ERROR);
        }

        String bucket = ossBucket;
        String bucketDomain = ossBucketDomain;


        String url = bucketDomain + "/" + fileName;
        if (ossType == 1) {
            //构造一个带指定 Region 对象的配置类
            Configuration cfg = new Configuration(Region.region0());
            //...其他参数参考类注释
            UploadManager uploadManager = new UploadManager(cfg);
            //...生成上传凭证，然后准备上传

            //如果是Windows情况下，格式是 D:\\qiniu\\test.png
            String localFilePath = localFile.getAbsolutePath();
            //默认不指定key的情况下，以文件内容的hash值作为文件名
            String key = fileName;
            Auth auth = Auth.create(ossAccessKey, ossSecretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(localFilePath, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        } else if (ossType == 2) {
            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(ossUrl, ossAccessKey, ossSecretKey);

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, localFile);

            // 上传文件。
            try {
                ossClient.putObject(putObjectRequest);
            } catch (OSSException | ClientException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            }
            // 关闭OSSClient。
            ossClient.shutdown();
        } else if (ossType == 3) {
            try {
                // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
//                MinioClient minioClient = new MinioClient("https://play.min.io", "Q3AM3UQ867SPQQA43P2F", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
                MinioClient minioClient = new MinioClient(ossUrl, ossAccessKey, ossSecretKey);

                // 使用putObject上传一个文件到存储桶中。
//                minioClient.putObject("asiatrip",fileName, localFile.getAbsolutePath(), new PutObjectOptions(PutObjectOptions.MAX_OBJECT_SIZE, PutObjectOptions.MIN_MULTIPART_SIZE));
                minioClient.putObject(bucket, fileName, localFile.getAbsolutePath(), new PutObjectOptions(file.getSize(), 0));
            } catch (MinioException e) {
                System.out.println("Error occurred: " + e);
                return RestResult.error(ERROR_SERVER_ERROR);
            } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            }
        } else if(ossType == 4) {
            //Todo 需要把文件上传到文件服务器。
        } else if(ossType == 5) {
            COSCredentials cred = new BasicCOSCredentials(ossAccessKey, ossSecretKey);
            ClientConfig clientConfig = new ClientConfig();
            String [] ss = ossUrl.split("\\.");
            if(ss.length > 3) {
                if(!ss[1].equals("accelerate")) {
                    clientConfig.setRegion(new com.qcloud.cos.region.Region(ss[1]));
                } else {
                    clientConfig.setRegion(new com.qcloud.cos.region.Region("ap-shanghai"));
                    try {
                        URL u = new URL(ossUrl);
                        clientConfig.setEndPointSuffix(u.getHost());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        return RestResult.error(ERROR_SERVER_ERROR);
                    }
                }
            }

            clientConfig.setHttpProtocol(HttpProtocol.https);
            COSClient cosClient = new COSClient(cred, clientConfig);

            try {
                cosClient.putObject(bucket, fileName, localFile.getAbsoluteFile());
            } catch (CosClientException e) {
                e.printStackTrace();
                return RestResult.error(ERROR_SERVER_ERROR);
            } finally {
                cosClient.shutdown();
            }
        }

        UploadFileResponse response = new UploadFileResponse();
        response.setUrl(url);
        return RestResult.ok(response);
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
    public RestResult listForegroundApplication() {
        Subject subject = SecurityUtils.getSubject();
        boolean fullInfo = subject.isAuthenticated() && subject.isPermitted("user:admin");

        List<PojoApplicationEntity> list = new ArrayList<>();
        applicationEntityRepository.findAllForegroundEntity().forEach(entity -> list.add(convertApplicationEntity(entity, fullInfo)));
        return RestResult.ok(list);
    }

    @Override
    public RestResult listBackgroundApplication() {
        Subject subject = SecurityUtils.getSubject();
        boolean fullInfo = subject.isAuthenticated() && subject.isPermitted("user:admin");

        List<PojoApplicationEntity> list = new ArrayList<>();
        applicationEntityRepository.findAllBackgroundEntity().forEach(entity -> list.add(convertApplicationEntity(entity, fullInfo)));
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
        entity.setBackground(pojoApplicationEntity.background);
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
        pojoApplicationEntity.background = entity.isBackground();
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
