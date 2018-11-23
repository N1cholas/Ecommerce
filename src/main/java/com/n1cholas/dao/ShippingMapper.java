package com.n1cholas.dao;

import com.n1cholas.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByUserIdShippingId(
            @Param("userId") Integer userId,
            @Param("shippingId") Integer shippingId
    );

    int updateByShipping(Shipping record);

    Shipping selectByUserIdShippingId(
            @Param("userId") Integer userId,
            @Param("shippingId") Integer shippingId
    );

    List<Shipping> selectByUserId(Integer userId);
}