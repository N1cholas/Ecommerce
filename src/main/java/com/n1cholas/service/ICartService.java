package com.n1cholas.service;

import com.n1cholas.common.ServerResponse;
import com.n1cholas.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> deleteProduct(Integer userId, String productIds);

    ServerResponse<CartVo> getList(Integer userId);

    ServerResponse<CartVo> selectOrUnselectAll (Integer userId, Integer checked, Integer productId);

    ServerResponse getCartProductCount(Integer userId);
}
