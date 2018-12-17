package com.n1cholas.controller.portal;

import com.n1cholas.common.Const;
import com.n1cholas.common.ResponseCode;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.User;
import com.n1cholas.service.IUserService;
import com.n1cholas.util.CookieUtil;
import com.n1cholas.util.JsonUtil;
import com.n1cholas.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登陆
     * @param username 用户名
     * @param password 密码
     * @param session 用户信息
     * @return ServerResponse<User>
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
//            session.setAttribute(Const.CURRENT_USER,response.getData());
            CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            RedisPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    /**
     * 用户登出
     * @param session 用户session
     * @return ServerResponse<String>
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout (HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     * @param user 传递user对象
     * @return 注册成功user对象
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 检测用户名和邮箱是否不存在
     * @param str 用户名或邮箱字符串
     * @param type 检测类型
     * @return 响应结果，用isSuccess函数判断
     */
    @RequestMapping(value = "check_vaild.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkVaild(String str, String type) {
        return iUserService.checkVaild(str, type);
    }

    /**
     * 获取用户对象
     * @param session 用户session
     * @return 用户对象
     */
    @RequestMapping(value = "get_user_object.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserObject(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 获取用户忘记密码问题
     * @param username 用户名
     * @return 忘记密码问题
     */
    @RequestMapping(value = "get_forget_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> getForgetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 判断忘记密码问题的正确性
     * @param username 用户名
     * @param question 问题
     * @param answer 答案
     * @return forgetToken，用于重置密码
     */
    @RequestMapping(value = "check_question_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkQuestionAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 重置密码
     * @param username 用户名
     * @param newPwd 新密码
     * @param forgetToken token
     * @return 响应结果
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String username, String newPwd, String forgetToken) {
        return iUserService.resetPassword(username, newPwd, forgetToken);
    }

    /**
     * 登录用户重置密码
     * @param session 用户信息
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * @return 响应结果
     */
    @RequestMapping(value = "reset_password_by_login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPasswordByLogin(HttpSession session, String oldPwd, String newPwd) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }
        return iUserService.resetPasswordByLogin(oldPwd, newPwd,user);
    }

    /**
     * 更新用户信息
     * @param session 用户信息
     * @param user 用户对象
     * @return 响应结果
     */
    @RequestMapping(value = "update_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登陆");
        }

        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());

        ServerResponse<User> response = iUserService.updateUserInfo(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }

        return response;
    }

    /**
     * 获取用户信息
     * @param session 用户信息
     * @return 响应结果
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登陆");
        }

        return iUserService.getUserInfo(currentUser.getId());
    }
}
