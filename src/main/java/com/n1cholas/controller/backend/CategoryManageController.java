package com.n1cholas.controller.backend;

import com.n1cholas.common.Const;
import com.n1cholas.common.ResponseCode;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.User;
import com.n1cholas.service.ICategoryService;
import com.n1cholas.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //todo 存在可添加重复的类别
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //添加品类
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session, String categoryName, Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新品类
            return iCategoryService.updateCategoryName(categoryName, categoryId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //不递归查询子节点
            return iCategoryService.getCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("get_deep_category_id.do")
    @ResponseBody
    public ServerResponse getDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //递归查询子节点
            return iCategoryService.getDeepCategoryId(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }
}
