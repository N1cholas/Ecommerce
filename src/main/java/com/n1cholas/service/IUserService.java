package com.n1cholas.service;

import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkVaild(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> resetPassword(String username, String newPwd, String forgetToken);

    ServerResponse<String> resetPasswordByLogin(String oldPwd, String newPwd, User user);

    ServerResponse<User> updateUserInfo(User user);

    ServerResponse<User> getUserInfo(Integer userId);
}
