package com.fduexchange.controller;

import com.fduexchange.pojo.UserInformation;
import com.fduexchange.pojo.UserPassword;
import com.fduexchange.response.BaseResponse;
import com.fduexchange.service.UserInformationService;
import com.fduexchange.service.UserPasswordService;
import com.fduexchange.tool.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@Controller
public class RegisterController {
    @Resource
    private UserPasswordService userPasswordService;

    @Resource
    private UserInformationService userInformationService;

    //开始注册用户
    @RequestMapping("/insertUser.do")
    @ResponseBody
    public BaseResponse insertUser(HttpServletRequest request,@RequestParam String uname,@RequestParam String phone,
                                   @RequestParam String password, @RequestParam String token) {
        String insertUserToken = (String) request.getSession().getAttribute("token");
        //防止重复提交
        if (StringUtils.getInstance().isNullOrEmpty(insertUserToken) || !insertUserToken.equals(token)) {
            return BaseResponse.fail();
        }
        //判断手机号码是否为正确
        if (!StringUtils.getInstance().isPhone(phone)) {
            return BaseResponse.fail();
        }
        //该手机号码已经存在
        int uid = userInformationService.selectIdByPhone(phone);
        if (uid != 0) {
            return BaseResponse.fail();
        }

        //用户信息
        UserInformation userInformation = new UserInformation();
        userInformation.setPhone(phone);
        userInformation.setCreatetime(new Date());
        userInformation.setUsername(uname);
        userInformation.setModified(new Date());
        int result;
        result = userInformationService.insertSelective(userInformation);
        //如果用户基本信息写入成功
        if (result == 1) {
            uid = userInformationService.selectIdByPhone(phone);
            String newPassword = StringUtils.getInstance().getMD5(password);
            UserPassword userPassword = new UserPassword();
            userPassword.setModified(new Date());
            userPassword.setUid(uid);
            userPassword.setPassword(newPassword);
            result = userPasswordService.insertSelective(userPassword);
            //密码写入失败
            if (result != 1) {
                userInformationService.deleteByPrimaryKey(uid);
                return BaseResponse.fail();
            } else {
                //注册成功
                userInformation = userInformationService.selectByPrimaryKey(uid);
                request.getSession().setAttribute("userInformation", userInformation);
                return BaseResponse.success();
            }
        }
        return BaseResponse.fail();
    }
}

