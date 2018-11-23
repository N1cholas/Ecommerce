package com.n1cholas.service;

import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.Shipping;

public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse get(Integer userId, Integer shippingId);

    ServerResponse list(Integer userId, Integer pageNum, Integer pageSize);
}
