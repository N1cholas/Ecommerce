package com.n1cholas.controller.common;

import com.n1cholas.common.Const;
import com.n1cholas.pojo.User;
import com.n1cholas.util.CookieUtil;
import com.n1cholas.util.JsonUtil;
import com.n1cholas.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionExpireFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(userJsonStr, User.class);
            if (user != null) {
                RedisPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
