package com.silent.config;

import com.alibaba.nacos.api.exception.NacosException;
import com.silent.nacos.NacosClient;
import com.silent.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/27.
 * @description:
 */
@Slf4j
@Component
public class AppConfig implements ApplicationListener<ServletWebServerInitializedEvent> {
	private static Map<String, String> nacosConfig = new HashMap<>();

	/**
	 * ==============================
	 */
	public static boolean isKey(String key, boolean defaultVal) {
		String dVal = defaultVal ? "Y" : "N";
		String val = get(key, dVal).toUpperCase().trim();
		return "Y".equals(val);
	}

	public static double get(String key, double defaultVal) {
		String value = get(key);
		if (null == value) {
			return defaultVal;
		}
		return Double.parseDouble(value);
	}

	public static long get(String key, long defaultVal) {
		String value = get(key);
		if (null == value) {
			return defaultVal;
		}
		return Long.parseLong(value);
	}

	public static int get(String key, int defaultVal) {
		String value = get(key);
		if (null == value) {
			return defaultVal;
		}
		return Integer.parseInt(value);
	}

	/**
	 * 将json类型的value解析成类型 T 的实例
	 *
	 * @param key
	 * @param clz
	 * @param <T>
	 * @return
	 */
	public static <T> T get(String key, Class<T> clz) {
		String value = get(key);
		if (null == value) {
			return null;
		}
		return GsonUtils.fromJson(value, clz);
	}

	public static String get(String key, String defaultVal) {
		String value = get(key);
		if (null == value) {
			return defaultVal;
		}
		return value;
	}

	public static String get(String key) {
		return nacosConfig.get(key);
	}

	/**
	 * ==============================
	 */

	public static void refreshNacosConfig(String content) {
		if (StringUtils.isBlank(content)) {
			nacosConfig = new HashMap<>();
			return;
		}

		Pattern pattern = Pattern.compile("\n|\r\n");
		String[] lines = content.split(pattern.toString());
		nacosConfig = getConfigsFromLines(lines);

		if (MapUtils.isEmpty(nacosConfig)) {
			return;
		}

		// 更新system property
		for (Map.Entry<String, String> entry : nacosConfig.entrySet()) {
			if (null == entry || null == entry.getKey()) {
				continue;
			}
			System.setProperty(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 解析key=value
	 *
	 * @param lines
	 * @return
	 */
	private static Map<String, String> getConfigsFromLines(String[] lines) {
		Map<String, String> configTmp = new HashMap<>(lines.length);
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("#")) {
				continue;
			}
			int index = line.indexOf("=");
			if (index < 0) {
				continue;
			}
			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();
			if (StringUtils.isBlank(key)) {
				continue;
			}
			configTmp.put(key, value);
		}
		return configTmp;
	}

	@Override
	public void onApplicationEvent(ServletWebServerInitializedEvent event) {
		ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
		try {
			NacosClient.instance().init(environment);
		} catch (NacosException e) {
			log.error("load config error.", e);
		}
	}
}