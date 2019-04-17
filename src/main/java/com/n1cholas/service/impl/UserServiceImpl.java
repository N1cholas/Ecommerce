package com.n1cholas.service.impl;

import com.n1cholas.common.Const;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.common.TokenCache;
import com.n1cholas.dao.UserMapper;
import com.n1cholas.pojo.User;
import com.n1cholas.service.IUserService;
import com.n1cholas.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

//用户接口实现类
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    //登录实现
    @Override
    public ServerResponse<User> login(String username, String password) {
        //判断用户名是否不存在
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //MD5密码登录
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        //判断密码是否错误
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        //置空密码
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登陆成功", user);
    }

    //注册实现
    @Override
    public ServerResponse<String> register(User user) {
        //判断用户名是否存在
        ServerResponse<String> vaildResponse = this.checkVaild(user.getUsername(), Const.USERNAME);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }

        //判断email是否存在
        vaildResponse = this.checkVaild(user.getEmail(), Const.EMALL);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }

        //设置角色
        user.setRole(1);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //判断用户数据是否插入成功
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    //校验实现
    @Override
    public ServerResponse<String> checkVaild(String str, String type) {
        //type不为空才开始校验
        if (StringUtils.isNotBlank(type)) {
            //校验用户名
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            //校验邮箱
            if (Const.EMALL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //获取找回问题实现
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        //判断用户名是否不存在
        ServerResponse<String> vaildResponse = this.checkVaild(username, Const.USERNAME);
        if (vaildResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (!(StringUtils.isNotBlank(question))) {
            return ServerResponse.createByErrorMessage("没有设置找回密码");
        }

        return ServerResponse.createBySuccess(question);
    }

    //校验问题是否正确实现
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("答案错误");
        }

        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);

        return ServerResponse.createBySuccess(forgetToken);
    }

    //重置密码
    public ServerResponse<String> resetPassword(String username, String newPwd, String forgetToken) {
        // 判断用户token是否不存在
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("没有用户token");
        }

        //判断用户名是否不存在
        ServerResponse<String> vaildResponse = this.checkVaild(username, Const.USERNAME);
        if (vaildResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //判断token是否无效
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或过期");
        }

        if (StringUtils.equals(forgetToken, token)) {
            String md5pwd = MD5Util.MD5EncodeUtf8(newPwd);
            int rowCount = userMapper.updatePasswordByUsername(username, md5pwd);

            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    //登录后重置密码
    public ServerResponse<String> resetPasswordByLogin(String oldPwd, String newPwd, User user) {
        //注意横向越权
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPwd), user.getId());

        //判断密码是否错误
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        //更新用户信息
        user.setPassword(MD5Util.MD5EncodeUtf8(newPwd));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }

        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    public ServerResponse<User> updateUserInfo(User user) {
        //username不更新
        //email校验，不能有相同的email
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("Email已经被注册");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新信息成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("更新信息失败");
    }

    public ServerResponse<User> getUserInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("没有该用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend
    //校验是否为管理员
    public ServerResponse<String> checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
