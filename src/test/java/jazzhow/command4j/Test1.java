package jazzhow.command4j;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Test1 {

    @Test
    void test2() throws Exception {
        String cmd = "D:/work/ffmpeg-win64-static/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/test123HD";
        CommandManager test = new CommandManager();
        test.exec("test", cmd);
        TimeUnit.SECONDS.sleep(10);
        Process process = test.destroy("test");
        System.out.println(process.isAlive());

        System.in.read();
    }

    @Test
    void test1() throws Exception {
        String cmd = "D:/work/ffmpeg-win64-static/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/test123HD";
        CommandManager test = new CommandManager();
        test.exec("test", cmd);
        test.exec("test", cmd);
        TimeUnit.SECONDS.sleep(10);
        test.destroy("test");
        test.destroy("test");
        System.in.read();

    }
}
