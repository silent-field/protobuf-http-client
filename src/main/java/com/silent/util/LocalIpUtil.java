package com.silent.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @date 2021/4/27.
 * @description:
 */
public class LocalIpUtil {
	/**
	 * 本地网卡IP列表
	 *
	 * @return
	 * @throws IOException
	 */
	public static List<String> getLocalIPList() {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface networkInterface = null;
			Enumeration<InetAddress> inetAddresses = null;
			InetAddress inetAddress = null;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					inetAddress = inetAddresses.nextElement();
					if (inetAddress != null && inetAddress instanceof Inet4Address) {
						if (!inetAddress.isLoopbackAddress()
								&& inetAddress.getHostAddress().indexOf(":") == -1) {
							ipList.add(inetAddress.getHostAddress());
						}
					}
				}
			}
		} catch (SocketException e) {
			return Collections.emptyList();
		}

		return ipList;
	}

	/**
	 * 获取外网ip
	 *
	 * @return
	 */
	public static String getExtranetIp() {
		Runtime runtime = Runtime.getRuntime();
		Process process;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			process = runtime.exec("curl http://icanhazip.com/");
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			String line;
			while ((line = br.readLine()) != null) {
				return line;
			}
			return StringUtils.EMPTY;
		} catch (Exception e) {
			return StringUtils.EMPTY;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(isr);
			IOUtils.closeQuietly(br);
		}
	}
}
