package com.n1cholas.controller.backend;

import com.github.pagehelper.PageInfo;
import com.n1cholas.common.Const;
import com.n1cholas.common.ResponseCode;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.User;
import com.n1cholas.service.IOrderService;
import com.n1cholas.service.IUserService;
import com.n1cholas.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(
            HttpSession session,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iOrderService.manageList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iOrderService.manageDetail(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> search(
            HttpSession session,
            Long orderNo,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iOrderService.manageSearch(orderNo, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("send.do")
    @ResponseBody
    public ServerResponse send(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iOrderService.manageSend(orderNo);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }
}
