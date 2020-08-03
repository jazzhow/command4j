package com.github.jazzhow.command4j;

import com.github.jazzhow.command4j.exceptions.ProcessExistException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class CommandManagerTest {

    @Test
    public void test1() throws IOException, InterruptedException, ProcessExistException {
        //可执行的linux程序，或windows程序，cmd1 与 cmd2执行的内容可以相同可以不同。
        String cmd1 = "D:/software/ffmpeg/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/test123HD";
        String cmd2 = "D:/software/ffmpeg/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/test123HD";
        CommandManager commandManager = new CommandManager();
        //第一个参数是这个启动进程的标识,在同一个CommandManager对象中唯一，它的意义是方便通过这个标识关闭这个启动的进程，
        //如果在同一个CommandManager对象中启动两个相同标识进程，那么会抛出一个ProcessExistException异常。
        commandManager.exec("process1", cmd1);
        CommandProcess process2 = commandManager.exec("process2", cmd2);
        //获取commandManager中的所有的CommandProcess
        Collection<CommandProcess> allProcess = commandManager.getAllProcess();
        System.out.println(allProcess.size());
        //process2的进程状态
        System.out.println(process2.getProcess().isAlive());
        //process2的执行命令
        System.out.println(process2.getCommand());
        //process2的执行时间
        System.out.println(process2.getExecTime());
        //process2的执行标识
        System.out.println(process2.getProcessId());
        TimeUnit.SECONDS.sleep(1);
        //关闭process1
        commandManager.destroy("process1");
        //关闭process2
        commandManager.destroy(process2.getProcessId());
        //关闭一个不存在的标识
        CommandProcess huhuhah = commandManager.destroy("呼呼哈哈");
        System.out.println("destroy传入commandManager中不存在的标识返回： " + huhuhah);
        if (!process2.getProcess().isAlive()){
            //process2的执行命令后系统的显示
            process2.getResponse().forEach(System.out::println);
        }
    }
}
