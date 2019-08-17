# command4j java命令执行管理器
 ## 作用
 调用系统的一些可执行软件，比如linux的shell脚本，windows的bat、exe可执行文件
 ## 为什么要创建此项目
 当我们需要用java调用本地程序时，使用Runtime.getRuntime().exec(cmd)固然可以，但需要注意需要此方法返回的process
 中获取inputStream、errorStream，需要使用两个线程从中读取内容， 如果忽略这个问题，会导致所执行的本地程序阻塞，
 开起来就是不报错停在那里了,原因是java中会为所执行的命令开辟一个缓冲区，用于存放命令所产生的输出，报错标准输出，
 和错误输出，如果被调用的程序产生的输出较多，而且也不从缓冲中读走，那么时间长了就会导致被调用的程序阻塞。
 所以本项目就是为了解决此问题，而且不需要手动对 inputStream、errorStream的流进行处理，使用commandManager.exec("一个命令")即可，
 commandManager会自动帮你处理inputStream、errorStream的流,从而避免被调用的程序阻塞。
 ## 使用方法，以junit为例
    @Test
    void test1() throws IOException, InterruptedException, ProcessExistException {
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
    }
 ## 如何让被调用的程序输出或关闭输出
 本项目使用slf4j控制被调用的程序的输出，指定jazzhow.command4j的日志级别即可，level=debug可以看到被调用的程序的输出的输出，
 level=info只输出关键信息，等于其他请自行脑补。
 
 ## 说明
 一个CommandManager对象可以管理多个运行的的本地程序，包括启动多个本地程序，关闭通过它启动的程序，
 在程序退出时会自动关闭所有通过他运行的程序。
 
 创建一个命令管理对象： CommandManager commandManager = new CommandManager();
 
 运行本地程序： commandManager.exec("进程标识", "启动命令"); //返回一个 java.lang.Process对象。
 
 结束通过commandManager启动的本地程序： commandManager.destroy("进程标识"); //返回一个 java.lang.Process对象。
 //commandManager如果进程标识村存在于commandManager中，返回null
 
 获取通过commandManager启动的程序：commandManager.getAllProcess(); //返回Collection<CommandProcess>
 
 停止通过commandManager所启动的所有程序：commandManager.destroyAll(); //返回Collection<CommandProcess>,被关闭的CommandProcess的集合
  
 
 ## 注意
 1、当java程序结束时，commandManager所管理的本地进程会被关闭，如果想java退出也不关闭被调用的本地程序，
 请使用java的原生方式Runtime.getRuntime().exec(cmd)
 2、linux下如果要结束本java程序，kill java程序是可以的，但是不要用kill -9结束，否则commandManager所管理的本地进程将
 无法被commandManager自动关闭。
 
