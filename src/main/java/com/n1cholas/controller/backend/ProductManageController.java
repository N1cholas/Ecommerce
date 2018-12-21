package com.n1cholas.controller.backend;

import com.google.common.collect.Maps;
import com.n1cholas.common.Const;
import com.n1cholas.common.ResponseCode;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.Product;
import com.n1cholas.pojo.User;
import com.n1cholas.service.IFileService;
import com.n1cholas.service.IProductService;
import com.n1cholas.service.IUserService;
import com.n1cholas.util.CookieUtil;
import com.n1cholas.util.JsonUtil;
import com.n1cholas.util.PropertiesUtil;
import com.n1cholas.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse saveProduct(HttpServletRequest httpServletRequest, Product product) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpServletRequest httpServletRequest, Integer productId, Integer status) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("get_product_detail.do")
    @ResponseBody
    public ServerResponse getProductDetail(HttpServletRequest httpServletRequest, Integer productId) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("get_product_list.do")
    @ResponseBody
    public ServerResponse getProductList(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("search_product.do")
    @ResponseBody
    public ServerResponse searchProduct(
            HttpServletRequest httpServletRequest,
            String productName,
            Integer productId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpServletRequest httpServletRequest, MultipartFile file, HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }

        // 是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFilename = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFilename;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFilename);
            fileMap.put("url", url);
            return ServerResponse.createBySuccess(fileMap);
        } else {
            return ServerResponse.createByErrorMessage("非管理员操作");
        }
    }

    @RequestMapping("upload_richtxt_img.do")
    @ResponseBody
    public Map uploadRichtxtImg(
            HttpServletRequest httpServletRequest,
            MultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Map resultMap = Maps.newHashMap();

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            resultMap.put("success", false);
            resultMap.put("msg", "用户未登录");
            return resultMap;
        }

        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请管理员登录后使用");
            return resultMap;
        }

        // 是否是管理员
        // 按照simditor的要求返回
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //业务逻辑
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFilename = iFileService.upload(file, path);
            if (StringUtils.isBlank(targetFilename)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }

            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFilename;

            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);

            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");

            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "非管理员操作");
            return resultMap;
        }
    }
}
