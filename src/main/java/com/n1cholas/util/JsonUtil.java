package com.n1cholas.util;

import com.google.common.collect.Lists;
import com.n1cholas.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象所以字段列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //统一日期格式 yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略在json字符串存在但java对象中不存在属性的错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2String (T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    public static <T> String obj2StringPretty (T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return obj instanceof String ?
                    (String) obj :
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    public static <T> T string2Obj (String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }

        try {
            return clazz.equals(String.class) ?
                    (T) str :
                    objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.warn("Parse String to object error", e);
            return null;
        }
    }

    public static <T> T string2Obj (String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }

        try {
            return (T) (typeReference.getType().equals(String.class) ?
                    str :
                    objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            log.warn("Parse String to object error", e);
            return null;
        }
    }

    public static <T> T string2Obj (String str, Class<?> collectionClass, Class<?>... eleClazzs) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, eleClazzs);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            log.warn("Parse String to object error", e);
            return null;
        }
    }


    public static void main(String[] args) {
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("test@test.com");

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("test2@test2.com");

        String json1 = obj2String(u1);
        String json2 = obj2StringPretty(u1);

        log.info("json1: {}", json1);
        log.info("json2: {}", json2);

        User user = string2Obj(json1, User.class);

        List<User> userList = Lists.newArrayList();
        userList.add(u1);
        userList.add(u2);

        String json3 = obj2String(userList);

//        userList = string2Obj(json3, new TypeReference<List<User>>() {
//        });

        userList = string2Obj(json3, List.class, User.class);

        System.out.println("end");
    }
}
