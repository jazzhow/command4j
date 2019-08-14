package jazzhow.command4j;

import jazzhow.command4j.model.MyProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private ConcurrentHashMap<String, MyProcess> processMap = new ConcurrentHashMap<>();

    public CommandManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::destroyAll));
    }

    @Override
    protected void finalize() throws Throwable {
        this.destroyAll();
        super.finalize();
    }

    /**
     * 使用代码同步防止同一id下的命令启动多次
     *
     * @param processId
     * @param command
     * @return
     */
    public synchronized void exec(String processId, String command) throws IOException {
        //先判断是否已存在此程序
        if (getProcess(processId) == null) {
            Process process = Runtime.getRuntime().exec(command);
            MyProcess myProcess = new MyProcess(processId, process, processMap);
            processMap.put(processId, myProcess);
            LOGGER.info("已启动程序" + processId);
        } else {
            LOGGER.warn("已经存在此id " + processId + "对应的程序，请更换id再启动");
        }
    }

    /**
     * 停止任务
     *
     * @param processId
     * @return
     */
    public synchronized void destroy(String processId) {
        LOGGER.info("正在关闭程序" + processId);
        MyProcess myProcess = processMap.get(processId);
        if (myProcess != null) {
            myProcess.setNormalExit(true);
            Process process = myProcess.getProcess();
            if (process.isAlive()) {
                process.destroy();
                processMap.remove(processId);
            } else {
                LOGGER.info("该程序程序" + processId + "已处于关闭状态，无需再次关闭");
            }
        } else {
            LOGGER.info("无法关闭不存在的程序" + processId);
        }
    }

    /**
     * 停止全部任务
     *
     * @return
     */
    public synchronized int destroyAll() {
        ArrayList<String> ids = new ArrayList<>();
        processMap.forEach((processId, myProcess) -> {
            myProcess.getProcess().destroy();
            ids.add(processId);
        });
        for (String id : ids) {
            processMap.remove(id);
        }
        return ids.size();
    }


    public MyProcess getProcess(String processId) {
        MyProcess myProcess = processMap.get(processId);
        return myProcess;
    }


    public Collection<MyProcess> getAllProcess() {
        Collection<MyProcess> values = processMap.values();
        return values;
    }

}
