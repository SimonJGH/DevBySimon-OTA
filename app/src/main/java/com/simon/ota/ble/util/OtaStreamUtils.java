package com.simon.ota.ble.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;

/**
 * 作者：${Simon} on 2016/10/29 0029 15:08
 * <p/>
 * 邮箱：2217403339@qq.com
 */
@SuppressWarnings("all")
public class OtaStreamUtils {

	/**
	 * convert InputStream to String
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len;
		while ((len = inputStream.read()) != -1) {
			baos.write(len);
		}
		String s = baos.toString();
		return s;
	}

	/**
	 * convert String to Stream
	 * 
	 * @param string
	 * @return
	 */
	public static InputStream convertStringToInputStream(String string) {
		InputStream is = new ByteArrayInputStream(string.getBytes());
		return is;
	}

	/**
	 * Convert byte[] to String
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public static String convertBytesToString(byte[] in) {
		InputStream is;
		try {
			is = convertByteToInputStream(in);
			return convertInputStreamToString(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * convert File to Stream
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static InputStream convertFileToStream(String path) throws Exception {
		File file = new File(path);
		InputStream is = new FileInputStream(file);

		// FileInputStream fis = new FileInputStream(file);
		// InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		return is;
	}

	/**
	 * Convert byte[] to file,such as: txt、doc、jpg... notable is that the
	 * fileName’s suffx must be contain ".txt" or ".jpg"
	 * 
	 * @param data
	 * @param filePath
	 * @return
	 */
	public static boolean convertBytesToFile(byte[] data, String filePath) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(filePath));
			bos.write(data, 0, data.length);
			bos.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * convert File to byte[]
	 * 
	 * @param filePath
	 * @return
	 */
	public static byte[] convertFileToByte(String filePath) {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;

		try {
			fis = new FileInputStream(filePath);
			baos = new ByteArrayOutputStream();
			int c = 0;
			byte[] buffer = new byte[1024 * 8];
			while ((c = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, c);
				baos.flush();
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static byte[] convertFileToByte(File file) {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;

		try {
			fis = new FileInputStream(file);
			baos = new ByteArrayOutputStream();
			int c = 0;
			byte[] buffer = new byte[1024 * 8];
			while ((c = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, c);
				baos.flush();
			}
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * (PrintWriter) Convert String to File，such as: txt、doc、pdf... notable is
	 * that the fileName’s suffx must be contain ".txt" or ".doc"
	 * 
	 * @param content
	 * @param fileName
	 * @return
	 */
	public static boolean convertStringToFile(String content, String fileName) {
		PrintWriter pWriter = null;
		try {
			pWriter = new PrintWriter(new FileWriter(fileName, true), true);
			pWriter.write(content);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			pWriter.close();
		}
		return false;
	}

	/**
	 * convert txt File to String
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String convertFileToString(String path) throws Exception {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String Str = null;
		String line = null;
		while ((line = br.readLine()) != null) {
			Str = Str + line;
		}
		return Str;
	}

	/**
	 * Convert InputStream to byte[]
	 * 
	 * @param is
	 * @return
	 */
	public static byte[] convertStreamToByte(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c = 0;
		byte[] buffer = new byte[8 * 1024];
		try {
			while ((c = is.read(buffer)) != -1) {
				baos.write(buffer, 0, c);
				baos.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}

	/**
	 * Convert byte[] to InputStream
	 * 
	 * @param in
	 */
	public static InputStream convertByteToInputStream(byte[] in)
			throws Exception {

		ByteArrayInputStream is = new ByteArrayInputStream(in);
		return is;
	}

	/**
	 * (String) copy file to another flie
	 * 
	 * @return
	 */
	private static void copyFileUsingFileChannels(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	/**
	 * whether file exists
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isExistFile(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * delete file
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		return file.delete();
	}

}
