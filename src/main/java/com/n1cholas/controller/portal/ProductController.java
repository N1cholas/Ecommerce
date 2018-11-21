package com.n1cholas.controller.portal;

import com.github.pagehelper.PageInfo;
import com.n1cholas.common.ServerResponse;
import com.n1cholas.service.IProductService;
import com.n1cholas.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @RequestMapping("get_product_detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        return iProductService.portalProductDetail(productId);
    }

    @RequestMapping("get_product_list_by_search.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductListBySearch(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderBy", defaultValue = "") String orderBy
    ) {
        return iProductService.getProductListByKeyword(keyword, categoryId, pageNum, pageSize, orderBy);
    }

}
