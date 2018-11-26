package com.n1cholas.dao;

import com.n1cholas.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByUserIdOrderNo(
            @Param("userId") Integer userId,
            @Param("orderNo") Long orderNo
    );

    Order selectByOrderNo(Long orderNo);

    List<Order> selectByUserId(Integer userId);

    List<Order> selectAll();
}