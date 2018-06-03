/**
 *
 * @Title:ToolsUtil.java
 * @Package:cn.com.szxJavaBook.tools
 * @Description:
 * @date:2018年6月1日下午8:43:39
 * @version:
 */
package cn.com.szxJavaBook.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import szx.constantPkg.CommonConstant;
import szx.constantPkg.FileHandleConstant;
import szx.fileHandlePkg.FileHandler;

/**
 * @ClassName:ToolsUtil
 * @Description:工具类
 * @author: shenzhangx
 * @date:2018年6月1日下午8:43:39
 * 
 */
public class ToolsUtil {
	// private static final String tempDir =
	// FileHandleConstant.E_盘+File.separator+FileHandleConstant.NEW+"0000666";
	/**
	 * @Title:extractCompress
	 * @Description:从压缩文件中提取某固定格式文件放在同级目录下
	 * @param srcPath
	 *            压缩文件路径（rar或zip格式）
	 * @param fileType
	 *            待提取文件的文件格式
	 * @return:void
	 * @throws Exception
	 */
	public static void extractCompress(String srcPath, String fileType)
			throws Exception {
		srcPath = FileHandler.dealFilePath(srcPath);
		File srcFile = new File(srcPath);
		if (!srcFile.exists())
			return;
		if (srcFile.isDirectory()) {
			File[] srcFiles = srcFile.listFiles();
			for (File file : srcFiles) {
				if (file.isDirectory()) {
					extractCompress(file.getAbsolutePath(), fileType);
				} else {
					if (file.getName().toLowerCase()
							.endsWith(FileHandleConstant.ZIP)) {
						extractZipFile(file, fileType);
					} else if (file.getName().toLowerCase()
							.endsWith(FileHandleConstant.RAR)) {
						extractRarFile(file, fileType);
					}
				}
			}
		} else {
			if (srcFile.getName().toLowerCase()
					.endsWith(FileHandleConstant.ZIP)) {
				extractZipFile(srcFile, fileType);
			} else if (srcFile.getName().toLowerCase()
					.endsWith(FileHandleConstant.RAR)) {
				extractRarFile(srcFile, fileType);
			}
		}
	}

	/**
	 * @Title:extractZipFile
	 * @Description:提取zip压缩包中某格式文件
	 * @param srcFile
	 *            压缩文件路径
	 * @param fileType
	 *            文件格式
	 * @return:void
	 * @throws Exception
	 */
	private static void extractZipFile(File srcFile, String fileType)
			throws Exception {
		// TODO Auto-generated method stub
		String fileName = srcFile.getName();
		ZipFile zipFile = new ZipFile(srcFile);
		Enumeration en = zipFile.getEntries();
		ZipEntry zipEntry = null;
		while (en.hasMoreElements()) {
			zipEntry = (ZipEntry) en.nextElement();
			if (!zipEntry.isDirectory()
					&& zipEntry.getName().toLowerCase().endsWith(fileType)) {
				fileName = zipEntry.getName();
				fileName = FileHandler.dealFilePath(fileName);
				if (fileName.contains(File.separator)) {
					fileName = fileName.substring(fileName
							.lastIndexOf(File.separator) + 1);
				}
				System.out.println("正在提取文件......" + fileName);
				File file = new File(srcFile.getParent() + File.separator
						+ fileName);
				file.createNewFile();
				InputStream in = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				try {
					int len;
					byte[] buff = new byte[FileHandleConstant.BUFFSIZE];
					while ((len = in.read(buff)) != -1) {
						fos.write(buff, 0, len);
					}
					fos.flush();
				} catch (Exception e) {
					throw e;
				} finally {
					if (in != null) {
						in.close();
					}
					if (fos != null) {
						fos.close();
					}
				}
			}
		}
		zipFile.close();// 如果不关，后续无法对此文件做操作
	}

	/**
	 * @Title:extractRarFile
	 * @Description:提取rar压缩包中某格式文件
	 * @param srcFile
	 *            压缩文件路径
	 * @param fileType
	 *            文件格式
	 * @return:void
	 * @throws Exception
	 */
	private static void extractRarFile(File srcFile, String fileType)
			throws Exception {
		// TODO Auto-generated method stub
		Archive archive = null;
		FileOutputStream fos = null;
		try {
			archive = new Archive(srcFile);
			FileHeader fh = archive.nextFileHeader();
			while (fh != null) {
				if (fh.isDirectory()) {
					fh = archive.nextFileHeader();
					continue;
				}
				String compressFileName = "";
				if (existZH(fh.getFileNameW())) {
					compressFileName = fh.getFileNameW().trim();
				} else {
					compressFileName = fh.getFileNameString().trim();
				}
				if (!compressFileName.toLowerCase().endsWith(fileType)) {
					fh = archive.nextFileHeader();
					continue;
				}
				compressFileName = FileHandler.dealFilePath(compressFileName);
				if (compressFileName.contains(File.separator)) {
					compressFileName = compressFileName
							.substring(compressFileName
									.lastIndexOf(File.separator) + 1);
				}
				System.out.println("正在提取文件......" + compressFileName);
				String newName = srcFile.getParent() + File.separator
						+ compressFileName;
				File tempFile = new File(newName);
				if (tempFile.exists()) {
					StringBuffer tempName = new StringBuffer(newName);
					tempName.insert(
							newName.lastIndexOf(FileHandleConstant.DOT),
							CommonConstant.Number_0);
					newName = tempName.toString();
				}
				fos = new FileOutputStream(newName);
				archive.extractFile(fh, fos);
				fos.close();
				fos = null;
				fh = archive.nextFileHeader();
			}
			archive.close();
			archive = null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
					fos = null;
				} catch (Exception e) {
				}
			}
			if (archive != null) {
				try {
					archive.close();
					archive = null;
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * @Title:compress
	 * @Description:此工具用于根据压缩文件中的文件名重命名压缩文件
	 * @param srcPath
	 *            压缩文件(rar或zip格式)
	 * @param fileType
	 *            待取文件名的文件类型(只取第一个文件名)
	 * @return:void
	 * @throws Exception
	 */
	public static void renameComFiles(String srcPath, String fileType)
			throws Exception {
		srcPath = FileHandler.dealFilePath(srcPath);
		File srcFile = new File(srcPath);
		if (!srcFile.exists())
			return;
		if (srcFile.isDirectory()) {
			File[] srcFiles = srcFile.listFiles();
			for (File file : srcFiles) {
				if (file.isDirectory()) {
					renameComFiles(file.getAbsolutePath(), fileType);
				} else {
					if (file.getName().toLowerCase()
							.endsWith(FileHandleConstant.ZIP)) {
						file.renameTo(new File(getZipName(file, fileType)));
					} else if (file.getName().toLowerCase()
							.endsWith(FileHandleConstant.RAR)) {
						file.renameTo(new File(getRarName(file, fileType)));
					}
				}
			}
		} else {
			if (srcFile.getName().toLowerCase()
					.endsWith(FileHandleConstant.ZIP)) {
				srcFile.renameTo(new File(getZipName(srcFile, fileType)));
			} else if (srcFile.getName().toLowerCase()
					.endsWith(FileHandleConstant.RAR)) {
				srcFile.renameTo(new File(getRarName(srcFile, fileType)));
			}
		}
	}

	/**
	 * @Title:getRarName
	 * @Description:获取新名称
	 * @param file
	 *            压缩文件(rar格式)
	 * @param fileType
	 *            待取文件名的文件类型(只取第一个文件名)
	 * @return:boolean
	 * @throws Exception
	 */
	private static String getRarName(File file, String fileType)
			throws Exception {
		// TODO Auto-generated method stub
		Archive archive = null;
		String newName = file.getName();
		try {
			archive = new Archive(file);
			FileHeader fh = archive.nextFileHeader();
			while (fh != null) {
				if (fh.isDirectory()) {
					fh = archive.nextFileHeader();
					continue;
				}
				String compressFileName = "";
				if (existZH(fh.getFileNameW())) {
					compressFileName = fh.getFileNameW().trim();
				} else {
					compressFileName = fh.getFileNameString().trim();
				}
				if (!compressFileName.toLowerCase().endsWith(fileType)) {
					fh = archive.nextFileHeader();
					continue;
				}
				compressFileName = FileHandler.dealFilePath(compressFileName);
				if (compressFileName.contains(File.separator)) {
					compressFileName = compressFileName.substring(
							compressFileName.lastIndexOf(File.separator) + 1,
							compressFileName
									.lastIndexOf(FileHandleConstant.DOT) + 1)
							+ FileHandleConstant.RAR;
				} else {
					compressFileName = compressFileName.substring(0,
							compressFileName
									.lastIndexOf(FileHandleConstant.DOT) + 1)
							+ FileHandleConstant.RAR;
				}
				System.out.println("正在重命名......" + compressFileName);
				newName = file.getParent() + File.separator + compressFileName;
				fh = archive.nextFileHeader();
				break;
			}
			archive.close();
			archive = null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (archive != null) {
				try {
					archive.close();
					archive = null;
				} catch (Exception e) {
				}
			}
		}
		return newName;
	}

	/**
	 * @Title:existZH
	 * @Description:判断路径是否带中文
	 * @param str
	 *            带判断的字符串
	 * @return:boolean
	 * @throws
	 */
	private static boolean existZH(String str) {
		String regEx = "[\\u4e00-\\u9fa5]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			return true;
		}
		return false;
	}

	/**
	 * @Title:getZipName
	 * @Description:获取新名称
	 * @param file
	 *            压缩文件(zip格式)
	 * @param fileType
	 *            待取文件名的文件类型(只取第一个文件名)
	 * @return:String
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private static String getZipName(File file, String fileType)
			throws Exception {
		// TODO Auto-generated method stub
		String fileName = file.getName();
		ZipFile zipFile = new ZipFile(file);
		Enumeration en = zipFile.getEntries();
		ZipEntry zipEntry = null;
		while (en.hasMoreElements()) {
			zipEntry = (ZipEntry) en.nextElement();
			if (!zipEntry.isDirectory()
					&& zipEntry.getName().toLowerCase().endsWith(fileType)) {
				fileName = zipEntry.getName();
				break;
			}
		}
		zipFile.close();// 如果不关，后续无法对此文件做操作
		fileName = FileHandler.dealFilePath(fileName);
		if (fileName.contains(File.separator)) {
			fileName = fileName.substring(
					fileName.lastIndexOf(File.separator) + 1,
					fileName.lastIndexOf(FileHandleConstant.DOT) + 1)
					+ FileHandleConstant.ZIP;
		} else {
			fileName = fileName.substring(0,
					fileName.lastIndexOf(FileHandleConstant.DOT) + 1)
					+ FileHandleConstant.ZIP;
		}
		System.out.println("正在重命名......"
				+ (file.getParent() + File.separator + fileName));
		return file.getParent() + File.separator + fileName;
	}

	/**
	 * @Title:cutFile
	 * @Description:剪切移动文件（含文件夹）
	 * @param srcPath 源文件路径
	 * @param destPath 目标文件目录路径
	 * @return:void
	 * @throws Exception 
	 */
	public static void cutFile(String srcPath, String destPath) throws Exception {
		// TODO Auto-generated method stub
		srcPath = FileHandler.dealFilePath(srcPath);
		destPath = FileHandler.dealFilePath(destPath);
		File srcFile = new File(srcPath);
		if(!srcFile.exists()) return;
		File desFile = new File(destPath);
		if(!desFile.exists()) desFile.mkdirs();
		String cutName = srcFile.getName();
		if(srcFile.isDirectory()){
			File[] files = srcFile.listFiles();
			for(File file : files){
				if(file.isDirectory()){
					cutFile(file.getAbsolutePath(),destPath);
					FileHandler.deleteFile(file.getAbsolutePath());//删除源文件
				}else{
					cutName = destPath + File.separator +file.getName();
					if(!file.getAbsolutePath().equalsIgnoreCase(cutName))cutFile(file,new File(cutName));
				}
			}
		}else{
			cutName = destPath + File.separator +srcFile.getName();
			if(!srcFile.getAbsolutePath().equalsIgnoreCase(cutName)) cutFile(srcFile,new File(cutName));
		}
	}
	
	/**
	 * 
	 * @Title:cutFile
	 * @Description:剪切移动文件
	 * @param srcFile 源文件路径
	 * @param destFile 目标文件路径
	 * @return:void
	 * @throws Exception
	 */
	public static void cutFile(File srcFile, File destFile) throws Exception{  
		if(destFile.exists()) destFile.delete();//覆盖式 
		destFile.createNewFile();
        FileOutputStream fileOutputStream = null;  
        InputStream inputStream = null;  
        byte[] bytes = new byte[1024];  
        int temp = 0;  
        try {  
            inputStream = new FileInputStream(srcFile);  
            fileOutputStream = new FileOutputStream(destFile);  
            while((temp = inputStream.read(bytes)) != -1){  
                fileOutputStream.write(bytes, 0, temp);  
                fileOutputStream.flush();  
            }  
            System.out.println("正在剪切移动文件......"+srcFile.getName());
        } catch (FileNotFoundException e) {  
            throw e;  
        }catch (IOException e) {  
        	throw e;  
        }finally{  
            if (inputStream != null) {  
                try {  
                    inputStream.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
            if (fileOutputStream != null) {  
                try {  
                    fileOutputStream.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }
            srcFile.deleteOnExit();//在程序结束时删除文件
        }  
    }  

}
