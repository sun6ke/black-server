package com.example.controllers;

import com.example.Api;
import com.example.config.jsonviews.UserView;
import com.example.daos.UserRepo;
import com.example.models.RegistrationInfo;
import com.example.models.User;
import com.example.services.TokenService;
import com.example.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.Api.SUCCESS;
import static com.example.Api.UPDATE_TOKEN_FAILED;

@RestController
public class UserController {
    static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepo userRepo;

    @Autowired
    UserService userService;

    @Autowired
    TokenService tokenService;

    /**
     * 更新用户无效的 token
     *
     * @param phone
     * @return
     */
    @RequestMapping(value = "/api/token", method = RequestMethod.GET)
    public Api.Result getToken(@RequestParam String phone) {
        User user = userRepo.findOneByPhone(phone);
        if (user == null) return Api.result(UPDATE_TOKEN_FAILED);
        return Api.result(SUCCESS).param("token").value(tokenService.generateToken(user));
    }

    /**
     * 获取当前用户的头像，返回数据为PNG格式的图片，如果当前用户未设置头像
     * 则返回默认的头像
     *
     * @return 若成功则返回HTTP状态码OK并返回byte数组数据
     */
    @RequestMapping(value = "/api/profile/avatar", method = RequestMethod.GET)
    public ResponseEntity<?> getAvatar(Model model) {
        User user = userService.getCurrentUser();
        if (user != null && user.getProfile() != null && user.getProfile().getAvatar() != null) {
            model.addAttribute("user", user);
            byte[] avatar = user.getProfile().getAvatar();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(avatar.length);
            return new ResponseEntity<>(avatar, headers, HttpStatus.OK);
        }
        //TODO 使用非固定URI读取资源
        Resource resource = applicationContext.getResource("url:http://localhost:8080/images/defaultavatar.png");
        try {
            InputStream inputStream = resource.getInputStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            byte[] avatar = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(avatar.length);
            return new ResponseEntity<>(avatar, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 更新当前用户的头像，将用户上传的头像转换为PNG格式并储存
     *
     * @param file 图片文件
     * @return HTTP状态码OK、INTERNAL_SERVER_ERROR、BAD_REQUEST分别表示操作成功、
     * 服务器转换图片失败或上传的图像出错
     */
    @RequestMapping(value = "/api/profile/avatar", method = RequestMethod.POST)
    public ResponseEntity<?> setAvatar(MultipartFile file) {
        //TODO 扩展默认使用ImageIO导致的某些格式无法转换并调整图片尺寸
        User user = userService.getCurrentUser();
        if (user != null && !file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                InputStream inputStream = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                byte[] avatar = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                user.getProfile().setAvatar(avatar);
                userRepo.save(user);
                return new ResponseEntity<>(HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 获取当前用户的个人信息，不包含头像
     *
     * @return 用户的个人信息，不包含头像
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.GET)
    @JsonView(UserView.Profile.class)
    public ResponseEntity<?> getMyProfile() {
        User user = userService.getCurrentUser();
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 更新用户的个人信息，不包含头像
     *
     * @param updatedUser 新的用户个人信息，不含头像
     * @return HTTP状态码OK表示操作成功，BAD_REQUEST表示参数异常
     */
    @RequestMapping(value = "/api/profile", method = RequestMethod.PUT)
    public ResponseEntity<?> setMyProfile(@RequestBody User updatedUser){
        User user = userService.getCurrentUser();
        updatedUser.setRegInfo(user.getRegInfo());
        userRepo.save(updatedUser);
        return ResponseEntity.ok(user);
    }

    @RequestMapping(value = "/api/reginfo", method = RequestMethod.GET)
    public ResponseEntity<?> getMyRegInfo() {
        User user = userService.getCurrentUser();
        if (user != null) {
            RegistrationInfo registrationInfo = user.getRegInfo();
            return new ResponseEntity<>(registrationInfo, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/api/users/{userId}/profile", method = RequestMethod.GET)
    @JsonView(UserView.Profile.class)
    public ResponseEntity<?> getUserProfile(@PathVariable int userId) {
        User user = userRepo.findOne(userId);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/api/users/{userId}/profile/avatar", method = RequestMethod.GET)
    public ResponseEntity<?> getUserAvatar(@PathVariable int userId) {
        User user = userRepo.findOne(userId);
        if (user != null && user.getProfile() != null && user.getProfile().getAvatar() != null) {
            byte[] avatar = user.getProfile().getAvatar();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(avatar.length);
            return new ResponseEntity<>(avatar, headers, HttpStatus.OK);
        }
        //TODO 使用非固定URI读取资源
        Resource resource = applicationContext.getResource("url:http://localhost:8080/images/defaultavatar.png");
        try {
            InputStream inputStream = resource.getInputStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
            byte[] avatar = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(avatar.length);
            return new ResponseEntity<>(avatar, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/api/profile/friends", method = RequestMethod.GET)
    @JsonView(UserView.UserSummary.class)
    public ResponseEntity<?> getMyFriendList() {
        final PageRequest pageable = new PageRequest(0, 10, Sort.Direction.ASC, "id");
        //TODO 将查询朋友的功能真正实现
        Page<User> users = userRepo.findAll(pageable);
        if (users.getTotalElements() > 0) {
            List<User> userList = users.getContent();
            return new ResponseEntity<>(userList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/api/friend/num", method = RequestMethod.GET)
    @JsonView(UserView.Profile.class)
    public ResponseEntity<?> getFriendNumber() {
        Api.Result result = Api.result(SUCCESS).param("num").value(123);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/follow/num", method = RequestMethod.GET)
    public ResponseEntity<?> getFollowNumber() {
        Api.Result result = Api.result(SUCCESS).param("num").value(200);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/fan/num", method = RequestMethod.GET)
    public ResponseEntity<?> getFanNumber() {
        Api.Result result = Api.result(SUCCESS).param("num").value(456);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/likes/num", method = RequestMethod.GET)
    public ResponseEntity<?> getLikeNumber() {
        Api.Result result = Api.result(SUCCESS).param("num").value(789);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        logger.warn("Returning HTTP 400 Bad Request", e);
    }


}
