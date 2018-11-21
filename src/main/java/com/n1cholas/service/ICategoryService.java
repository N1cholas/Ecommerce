package com.n1cholas.service;

import com.n1cholas.common.ServerResponse;
import com.n1cholas.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName, Integer parentId);

    ServerResponse<String> updateCategoryName(String categoryName, Integer categoryId);

    ServerResponse<List<Category>> getCategory(Integer categoryId);

    ServerResponse<List<Integer>> getDeepCategoryId(Integer categoryId);
}
