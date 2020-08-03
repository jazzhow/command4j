package com.github.jazzhow.command4j;

import com.github.jazzhow.command4j.exceptions.ProcessExistException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private static final String SYSTEM_NAME = "os.name";
    private static final String SYSTEM_WIN = "WIN";
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
     * @param processId 启动进程的标识,在同一个CommandManager对象中唯一，
     *                  它的意义是方便通过这个标识关闭这个启动的进程,
     *                  如果在同一个CommandManager对象中启动两个相同标识进程，那么会抛出一个ProcessExistException异常。
     * @param command   调用本地程序的命令
     * @return 返回启动的程序
     * @throws ProcessExistException 启动进程的标识已存在
     * @throws IOException           启动命令路径有误
     */
    public synchronized CommandProcess exec(String processId, String command) throws ProcessExistException, IOException {
        StringBuilder commandBuilder = getStringBuilder(command);
        LOGGER.info("正在启动程序 " + processId);
        LOGGER.info("启动命令 " + commandBuilder.toString());

        //先判断是否已存在此程序
        if (getProcess(processId) == null) {
            Date now = new Date();
            Process process = Runtime.getRuntime().exec(commandBuilder.toString());
            CommandProcess commandProcess = new CommandProcess(
                    processId, process, now, commandBuilder.toString(), new ArrayList<>(), this);
            processMap.put(processId, commandProcess);
            return commandProcess;
        } else {
            throw new ProcessExistException("已经存在此id " + processId + "对应的程序，请更换id再启动");
        }
    }

    /**
     * 判断是window系统，还是linux。并添加对应系统的命令前缀
     *
     * @param command String
     * @return commandBuilder
     */
    private StringBuilder getStringBuilder(String command) {
        StringBuilder commandBuilder = new StringBuilder();
        if (System.getProperty(SYSTEM_NAME).toUpperCase().contains(SYSTEM_WIN)) {
            commandBuilder.append("cmd ").append("/c ");
        } else {
            commandBuilder.append("/bin/sh ").append("-c ");
        }
        commandBuilder.append(command);
        return commandBuilder;
    }

    /**
     * 停止任务
     *
     * @param processId 启动进程的标识,在同一个CommandManager对象中唯一，
     *                  它的意义是方便通过这个标识关闭这个启动的进程,
     *                  如果在同一个CommandManager对象中启动两个相同标识进程，那么会抛出一个ProcessExistException异常。
     * @return 返回启动的程序
     */
    public synchronized CommandProcess destroy(String processId) {
        LOGGER.info("正在关闭程序" + processId);
        CommandProcess commandProcess = processMap.get(processId);
        if (commandProcess != null) {
            commandProcess.setNormalExit(true);
            Process process = commandProcess.getProcess();
            if (process.isAlive()) {
                process.destroy();
                processMap.remove(processId);
            } else {
                LOGGER.info("该程序程序" + processId + "已处于关闭状态，无需再次关闭");
            }
            return commandProcess;
        } else {
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected synchronized CommandProcess removeFromProcessMap(String processId) {
        return processMap.remove(processId);
    }

    /**
     * 停止全部任务
     *
     * @return 被关闭的CommandProcess的集合
     * @throws Exception Exception
     */
    @SuppressWarnings("UnusedReturnValue")
    public synchronized List<CommandProcess> destroyAll() throws Exception {
        ArrayList<CommandProcess> destroyProcessList = new ArrayList<>();
        ArrayList<String> errList = new ArrayList<>();
        Collection<CommandProcess> allProcess = getAllProcess();
        allProcess.forEach(commandProcess -> {
            try {
                destroy(commandProcess.getProcessId());
                destroyProcessList.add(commandProcess);
            } catch (Exception e) {
                errList.add(e.getMessage());
            }
        });
        if (!errList.isEmpty()) {
            throw new Exception(StringUtils.join(errList, ","));
        }
        return destroyProcessList;
    }


    public CommandProcess getProcess(String processId) {
        return processMap.get(processId);
    }


    public Collection<CommandProcess> getAllProcess() {
        return processMap.values();
    }

}
