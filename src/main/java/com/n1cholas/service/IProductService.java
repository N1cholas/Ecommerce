package com.n1cholas.service;

import com.github.pagehelper.PageInfo;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.Product;
import com.n1cholas.vo.ProductDetailVo;

public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);

    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    ServerResponse<ProductDetailVo> portalProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductListByKeyword(
            String keyword,
            Integer categoryId,
            int pageNum,
            int pageSize,
            String orderBy
    );
}
