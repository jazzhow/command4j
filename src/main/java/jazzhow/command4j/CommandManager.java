package jazzhow.command4j;

import jazzhow.command4j.exceptions.ProcessExistException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);
    private ConcurrentHashMap<String, CommandProcess> processMap = new ConcurrentHashMap<>();

    public CommandManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                destroyAll();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }));
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
    public synchronized Process exec(String processId, String command) throws ProcessExistException, IOException {
        //先判断是否已存在此程序
        if (getProcess(processId) == null) {
            Process process = Runtime.getRuntime().exec(command);
            CommandProcess myProcess = new CommandProcess(processId, process, processMap);
            processMap.put(processId, myProcess);
            LOGGER.info("已启动程序" + processId);
            return process;
        } else {
            throw new ProcessExistException("已经存在此id " + processId + "对应的程序，请更换id再启动");
        }
    }

    /**
     * 停止任务
     *
     * @param processId
     * @return
     */
    public synchronized Process destroy(String processId) {
        LOGGER.info("正在关闭程序" + processId);
        CommandProcess myProcess = processMap.get(processId);
        if (myProcess != null) {
            myProcess.setNormalExit(true);
            Process process = myProcess.getProcess();
            if (process.isAlive()) {
                process.destroy();
                processMap.remove(processId);
            } else {
                LOGGER.info("该程序程序" + processId + "已处于关闭状态，无需再次关闭");
            }
            return process;
        } else {
            return null;
        }
    }

    /**
     * 停止全部任务
     *
     * @return
     */
    public synchronized List<Process> destroyAll() throws Exception {
        ArrayList<Process> destroyProcessList = new ArrayList<>();
        ArrayList<String> errList = new ArrayList<>();
        processMap.forEach((processId, myProcess) -> {
            try {
                myProcess.getProcess().destroy();
                destroyProcessList.add(myProcess.getProcess());
            } catch (Exception e) {
                errList.add(e.getMessage());
            }
        });
        for (Process id : destroyProcessList) {
            processMap.remove(id);
        }
        if (!errList.isEmpty()) {
            throw new Exception(StringUtils.join(errList, ","));
        }
        return destroyProcessList;
    }


    public CommandProcess getProcess(String processId) {
        CommandProcess myProcess = processMap.get(processId);
        return myProcess;
    }


    public Collection<CommandProcess> getAllProcess() {
        Collection<CommandProcess> values = processMap.values();
        return values;
    }

}
