package com.lhl.monitor;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 文件变化监听器
 * 在Apache的Commons-IO中有关于文件的监控功能的代码. 文件监控的原理如下：
 * 由文件监控类FileAlterationMonitor中的线程不停的扫描文件观察器FileAlterationObserver，
 * 如果有文件的变化，则根据相关的文件比较器，判断文件时新增，还是删除，还是更改。（默认为1000毫秒执行一次扫描）
 * Created by lihongli on 2019/3/7
 */
public class FileListener extends FileAlterationListenerAdaptor {

    private Logger logger = Logger.getLogger(FileListener.class);

    @Override
    public void onStart(FileAlterationObserver observer) {
        /*File directory = observer.getDirectory();
        File[] files = directory.listFiles();
        for (File file : files) {
            logger.info(file.getAbsolutePath());
        }*/
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }

    /**
     * 目录被创建
     * @param directory
     */
    @Override
    public void onDirectoryCreate(File directory) {
        logger.info("[目录新建]" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryChange(File directory) {
        logger.info("[目录修改]" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File directory) {
        logger.info("[目录删除]" + directory.getAbsolutePath());
    }

    /**
     * 文件创建
     * @param file
     */
    @Override
    public void onFileCreate(File file) {
        logger.info("[新建]" + file.getAbsolutePath());
        // 测试传输大文件 会提示 java.io.FileNotFoundException: D:\*** (另一个程序正在使用此文件，进程无法访问。)
        File file2 = new File("D:\\" + file.getName());
        try(FileInputStream fs = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(file2)) {
            byte[] bytes = new byte[1023];
            int count;
            while((count = fs.read(bytes)) != -1){
                fos.write(bytes, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件修改
     * @param file
     */
    @Override
    public void onFileChange(File file) {
        // 当传输大文件时 跨两个或以上轮训间隔时 传输完成时会触发修改
        logger.info("[修改]" + file.getAbsolutePath());
    }

    /**
     * 文件删除
     * @param file
     */
    @Override
    public void onFileDelete(File file) {
        logger.info("[删除]" + file.getAbsolutePath());
    }

    public static void main(String[] args){
        // 监控目录
        String dir = "D:\\conf";
        // 轮训间隔
        long interval = TimeUnit.SECONDS.toMillis(3);
        // 创建过滤器
        IOFileFilter directories = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);
        IOFileFilter files = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".iso"));
        IOFileFilter filter = FileFilterUtils.or(directories, FileFilterUtils.fileFileFilter());
        // 使用过滤器
        FileAlterationObserver observer = new FileAlterationObserver(new File(dir), filter);
        observer.addListener(new FileListener());
        // 创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        // 开始监控
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
