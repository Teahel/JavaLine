package com.example.bd_bot.common.utils;

import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author {l.t.j}
 * @Date: 2022/04/21
 */
public class ReturnResultUtils extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public ReturnResultUtils() {
		put("code", 0);
		put("msg", "success");
	}

	public static ReturnResultUtils error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}

	public static ReturnResultUtils error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}

	public static ReturnResultUtils apiError(int status, String msg) {
        ReturnResultUtils r = new ReturnResultUtils();
        r.put("code", HttpStatus.SC_OK);
        r.put("status", status);
        r.put("msg", msg);
        return r;
	}

	public static ReturnResultUtils error(int code, String msg) {
		ReturnResultUtils r = new ReturnResultUtils();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}

	public static ReturnResultUtils ok(String msg) {
		ReturnResultUtils r = new ReturnResultUtils();
		r.put("msg", msg);
		return r;
	}

	public static ReturnResultUtils ok(Map<String, Object> map) {
		ReturnResultUtils r = new ReturnResultUtils();
		r.putAll(map);
		return r;
	}

	public static ReturnResultUtils ok() {
		return new ReturnResultUtils();
	}



	@Override
	public ReturnResultUtils put(String key, Object value) {
		super.put(key, value);
		return this;
	}

    /**
     * 添加单个对象数据
     *
     * @param value 传入的值
     * @return 封装结果
     */
    public ReturnResultUtils putObject(Object value) {
        super.put("data", value);
        return this;
    }

    /**
     * 添加List对象
     *
     * @param value 传入的值
     * @return 封装结果
     */
    public ReturnResultUtils putList(Object value) {
        super.put("list", value);
        return this;
    }

    /**
     * 添加分页数据
     *
     * @param value 传入的值
     * @return 封装结果
     */
    public ReturnResultUtils putPage(Object value) {
        super.put("page", value);
        return this;
    }

    /**
     * 添加Map数据
     *
     * @param map 传入的值
     * @return 封装结果
     */
    public ReturnResultUtils putMap(Map<String, Object> map) {
        super.putAll(map);
        return this;
    }

    /**
     * 添加Map数据
     *
     * @param object 传入的bean对象封装的值
     * @return 封装结果
     */
    public ReturnResultUtils putMap(Object object) {
        super.putAll(BeanUtil.beanToMap(object, false, true));
        return this;
    }

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
