package com.n1cholas.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.n1cholas.common.Const;
import com.n1cholas.common.ResponseCode;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.User;
import com.n1cholas.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(user.getId(), orderNo, path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = Maps.newHashMap();

        Iterator it = requestParams.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String[] values = requestParams.get(key);
            String resultStr = "";
            for (int i = 0; i < values.length; i++) {
                resultStr = (i == values.length - 1) ? resultStr + values[i] : resultStr + values[i] + ",";
            }
            params.put(key, resultStr);
        }
        logger.info(
                "支付宝回调,sign:{},trade_status:{},参数:{}",
                params.get("sign"), params.get("trade_status"), params.toString()
        );

        //验证回调的正确性（是否是支付宝发送的）
        //避免重复通知
        params.remove("sign_type");
        try {
            boolean aplipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!aplipayRSACheckedV2) {
                return ServerResponse.createByErrorMessage("非法请求");
            }

        } catch (AlipayApiException e) {
            logger.error("支付宝回调验证异常", e);
        }

        //todo 验证各种数据

        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }

        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    @RequestMapping("polling_order_status.do")
    @ResponseBody
    public ServerResponse<Boolean> pollingOrderStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        return iOrderService.pollingOrderStatus(user.getId(), orderNo);
    }
}
