# command4j java命令执行管理器
 ## 作用
 调用系统的一些可执行软件，比如linux的shell脚本windows的bat、exe可执行文件
 
 ## 说明
java调用命令的工具
    @Test
    void test1() throws IOException, InterruptedException {
        String cmd = "D:/work/ffmpeg-win64-static/bin/ffmpeg -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov -f flv -r 25 -g 25 -s 640x360 -an rtmp://localhost/live/test -vcodec copy  -f flv -an rtmp://localhost/live/test123HD";
        CommandManager test = new CommandManager();
        test.exec("test", cmd);
        test.exec("test", cmd);
        TimeUnit.SECONDS.sleep(10);
        test.destroy("test");
        test.destroy("test");
        System.in.read();
