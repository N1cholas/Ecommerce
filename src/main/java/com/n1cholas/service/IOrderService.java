package com.n1cholas.service;

import com.github.pagehelper.PageInfo;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Integer userId, Long orderNo, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse<Boolean> pollingOrderStatus (Integer userId, Long orderId);

    ServerResponse createOrder (Integer userId, Integer shippingId);

    ServerResponse cancelOrder (Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct (Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNo);

    ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize);

    ServerResponse manageSend(Long orderNo);
}
