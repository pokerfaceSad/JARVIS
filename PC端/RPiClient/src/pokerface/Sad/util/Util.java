package pokerface.Sad.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Util {
	/**
	 *  获取配置文件
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties getProperties() throws FileNotFoundException,
			IOException {
		Properties pro = new Properties();
		pro.load(new FileInputStream("RPiClient.properties"));
		return pro;
	}

	/**
	 *  获取当前时间
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getDate() throws FileNotFoundException, IOException {
		Properties pro = null;
		String date = null;
		pro = getProperties();
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(
				pro.getProperty("dateFormat"));
		date = sdf.format(d);
		return date;
	}

	/**
	 * WOL唤醒PC
	 * @throws IOException
	 */
	public static void wakeOnLAN() throws IOException {
		int port = 1000;
		String macAddress = Util.getProperties().getProperty("macAddress"); //PC的mac地址
		String destIP = Util.getProperties().getProperty("destIP");// 广播地址
		// 检测 mac 地址,并将其转换为二进制
		byte[] destMac = getMacBytes(macAddress);
		if (destMac == null) {
			return;
		}

		InetAddress destHost = InetAddress.getByName(destIP);
		// construct packet data
		byte[] magicBytes = new byte[102];
		// 将数据包的前6位放入0xFF即 "FF"的二进制
		for (int i = 0; i < 6; i++) {
			magicBytes[i] = (byte) 0xFF;
		}

		// 从第7个位置开始把mac地址放入16次
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < destMac.length; j++) {
				magicBytes[6 + destMac.length * i + j] = destMac[j];
			}
		}
		// create packet
		DatagramPacket dp = null;
		dp = new DatagramPacket(magicBytes, magicBytes.length, destHost, port);
		DatagramSocket ds = new DatagramSocket();
		ds.send(dp);
		ds.close();
		System.out.println("ok");
	}

	private static byte[] getMacBytes(String macStr)
			throws IllegalArgumentException {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Invalid hex digit in MAC address.");
		}
		return bytes;
	}

}
