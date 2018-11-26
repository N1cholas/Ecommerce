package com.n1cholas.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.n1cholas.common.Const;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.dao.*;
import com.n1cholas.pojo.*;
import com.n1cholas.service.IOrderService;
import com.n1cholas.util.BigDecimalUtil;
import com.n1cholas.util.DateTimeUtil;
import com.n1cholas.util.FTPUtil;
import com.n1cholas.util.PropertiesUtil;
import com.n1cholas.vo.OrderItemVo;
import com.n1cholas.vo.OrderProductVo;
import com.n1cholas.vo.OrderVo;
import com.n1cholas.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    //pay
    public ServerResponse pay(Integer userId, Long orderNo, String path) {

        Map<String, String> resultMap = Maps.newHashMap();

        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }

        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        //支付宝的二维码生成
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题”
        String subject = new StringBuilder().append("mmall扫码支付,订单号:").append(order.getOrderNo()).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder()
                .append("订单")
                .append(outTradeNo)
                .append("购买商品总共")
                .append(totalAmount)
                .append("元")
                .toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        for(OrderItem orderItem : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(
                    orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity()
            );
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                //集成二维码
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                //注意“/”
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());

                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());

                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常", e);
                }

                logger.info("qrPath:" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();

                resultMap.put("qrUrl", qrUrl);

                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code：%s, msg：%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format(
                        "subCode：%s, subMsg：%s", response.getSubCode(),
                        response.getSubMsg())
                );
            }
            logger.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单");
        }

        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }

        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            logger.info("支付宝回调成功");
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();

        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPaltform.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse<Boolean> pollingOrderStatus (Integer userId, Long orderId) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单");
        }

        if (order.getStatus() >= Const.OrderStatus.PAID.getCode()) {
            return ServerResponse.createBySuccess(true);
        }

        return ServerResponse.createBySuccess(false);
    }





    //backend
    public ServerResponse<PageInfo> manageList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("没有该订单");
    }

    public ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);

        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));

            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("没有该订单");
    }

    public ServerResponse manageSend(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
           if (order.getStatus() == Const.OrderStatus.PAID.getCode()) {
               order.setStatus(Const.OrderStatus.SHIPPED.getCode());
               order.setSendTime(new Date());
               orderMapper.updateByPrimaryKeySelective(order);
               return ServerResponse.createBySuccessMessage("已发货");
           }
        }
        return ServerResponse.createByErrorMessage("没有该订单");
    }



    //portal
    public ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //管理员查询
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, order.getOrderNo());
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }

        return ServerResponse.createByErrorMessage("没有该订单");
    }

    public ServerResponse getOrderCartProduct (Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");

        for(OrderItem orderItem : orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }

        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse cancelOrder (Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        if (order.getStatus() != Const.OrderStatus.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("该订单已付款，无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatus.CANCELED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (row > 0) {
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    public ServerResponse createOrder (Integer userId, Integer shippingId) {
        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        //判断购物车是否为空
        ServerResponse<List<OrderItem>> serverResponse = getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }

        List<OrderItem> orderItemList = serverResponse.getData();
        BigDecimal payment = getOrderTotalPrice(orderItemList);

        //生成订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }

        //添加orderItem的订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);

        //减少库存
        reduceProductStock(orderItemList);

        //清空购物车
        cleanCart(cartList);

        //返回数据给前端
        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();

        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentType.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatus.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList) {
        for(Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();

        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentType.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatus.NO_PAY.getCode());

        //付款时间
        //发货时间

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }

        return null;
    }

    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        //校验购物车商品状态和数量
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //校验状态
            if (Const.ProductStatus.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("商品：" + product.getName() + "已下架");
            }
            //校验数量
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("商品：" + product.getName() + "库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));

            orderItemList.add(orderItem);
        }

        return ServerResponse.createBySuccess(orderItemList);
    }

}
