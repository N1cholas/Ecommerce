package com.n1cholas.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMALL = "email";

    public static final String USERNAME = "username";

    public interface ProductOrderBy {
        Set<String> PRICE_ASC_ESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Role {
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1; //管理员
    }

    public interface Cart {
        int CHECKED = 1;//选择状态
        int UN_CHECKED = 0;//未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatus {
        ON_SALE(1, "在售");

        private int code;
        private String value;

        ProductStatus(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
