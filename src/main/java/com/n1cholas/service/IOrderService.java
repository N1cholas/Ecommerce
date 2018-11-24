package com.n1cholas.service;

import com.n1cholas.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Integer userId, Long orderNo, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse<Boolean> pollingOrderStatus (Integer userId, Long orderId);
}
