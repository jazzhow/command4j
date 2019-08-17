package jazzhow.command4j;

import jazzhow.command4j.exceptions.ProcessExistException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class Test1 {

    @Test
    void test4() throws IOException, InterruptedException {
        String id = "test";
        String cmd = "D:/software/ffmpeg/bin/ffmpeg -i rtmp://58.200.131.2:1935/livetv/hunantv -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/" + id + " -vcodec copy  -f flv -an rtmp://localhost/live/" + id + "HD";
        Process exec = Runtime.getRuntime().exec(cmd);
        TimeUnit.SECONDS.sleep(10);
        exec.destroy();
        exec.destroy();
        exec.destroy();
        exec.destroy();
    }

    @Test
    void test3() throws IOException, InterruptedException, ProcessExistException {
        CommandManager test = new CommandManager();
        //ExecutorService fixedThreadPool = Executors.newFixedThreadPool(2);

        String id = "test";
        String cmd = "D:/software/ffmpeg/bin/ffmpeg -i rtmp://58.200.131.2:1935/livetv/hunantv -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/" + id + " -vcodec copy  -f flv -an rtmp://localhost/live/" + id + "HD";
        //test.destroy("test");
        {
            test.exec("test", cmd);
            Collection<CommandProcess> allProcess = test.getAllProcess();
            //System.out.println(allProcess);
        }
        {
            test.destroy("test");
            Collection<CommandProcess> allProcess = test.getAllProcess();
            //System.out.println(allProcess);
        }
        {
            test.exec("test", cmd);
            Collection<CommandProcess> allProcess = test.getAllProcess();
            System.out.println(allProcess);
        }
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            Collection<CommandProcess> allProcess = test.getAllProcess();
            System.out.println(allProcess);
        }
    }

    @Test
    void test2() throws Exception {
        String cmd = "D:/work/ffmpeg-win64-static/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/testHD";
        CommandManager test = new CommandManager();
        test.exec("test", cmd);
        TimeUnit.SECONDS.sleep(10);
        Process process = test.destroy("test");
        if (process != null) {
            System.out.println(process.isAlive());
        }

        System.in.read();
    }

    @Test
    void test1() throws Exception {
        String cmd = "D:/work/ffmpeg-win64-static/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/testHD";
        CommandManager test = new CommandManager();
        test.exec("test", cmd);
        test.exec("test", cmd);
        TimeUnit.SECONDS.sleep(10);
        test.destroy("test");
        test.destroy("test");
        System.in.read();

    }
}
