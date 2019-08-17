# command4j java命令执行管理器
 ## 作用
 调用系统的一些可执行软件，比如linux的shell脚本，windows的bat、exe可执行文件
 ## 为什么要创建此项目
 使用Runtime.getRuntime().exec(cmd)固然可以，但需要注意需要此方法返回的process获取inputStream、errorStream，需要使用两个线程从中读取内容，
 如果忽略这个问题，会导致所执行的本地程序阻塞，开起来就是不报错停在那里了,原因是java中会为所执行的命令开辟一个缓冲区，用于存放命令所产生的输出，报错标准输出，
 和错误输出，如果被调用的程序产生的输出较多，而且也不从缓冲中读走，那么时间长了就会导致被调用的程序阻塞。
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
