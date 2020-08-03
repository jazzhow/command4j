package com.github.jazzhow.command4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcess.class);
    private volatile boolean normalExit = false;
    private volatile boolean ioStop = false;
    private Process process;
    private String processId;
    /**
     * 执行时间
     */
    private Date execTime;
    /**
     * 执行命令
     */
    private String command;

    /**
     * 执行命令后系统显示的内容
     */
    private List<String> response;

    private static ExecutorService executorService;

    static {
        executorService = new ThreadPoolExecutor(3, 5, 10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName(r.getClass().getName());
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    protected CommandProcess(String processId,
                             Process process,
                             Date execTime,
                             String command,
                             List<String> response,
                             CommandManager commandManager) {
        this.processId = processId;
        this.process = process;
        this.execTime = execTime;
        this.command = command;
        this.response = response;
        executorService.execute(() -> executeBfReader(process.getErrorStream()));
        executorService.execute(() -> executeBfReader(process.getInputStream()));
        executorService.execute(() -> executeProcessWaitFor(processId, process, commandManager));
    }

    private void executeProcessWaitFor(String processId, Process process, CommandManager commandManager) {
        Integer processExitValue = null;
        try {
            processExitValue = process.waitFor();
        } catch (InterruptedException e) {
            process.destroy();
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (processExitValue != null) {
                if (normalExit) {
                    LOGGER.info("程序" + processId + "被关闭，返回代码: " + processExitValue);
                } else {
                    commandManager.removeFromProcessMap(processId);
                    if (processExitValue.equals(0)) {
                        LOGGER.info("程序" + processId + "运行完毕，返回代码: " + processExitValue);
                    } else {
                        LOGGER.warn("程序" + processId + "异常退出，返回代码: " + processExitValue);
                    }
                }
            } else {
                LOGGER.error("程序" + processId + "意外退出");
                commandManager.removeFromProcessMap(processId);
            }
        }
    }

    private void executeBfReader(InputStream process) {
        String line;
        try (
                BufferedReader errBfReader = new BufferedReader(new InputStreamReader(process));
        ) {
            while (!ioStop && (line = errBfReader.readLine()) != null) {
                response.add(line);
                LOGGER.debug(line);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void shutdownExecutor() {
        executorService.shutdown();
    }

    protected void setNormalExit(boolean normalExit) {
        this.normalExit = normalExit;
    }

    public Process getProcess() {
        return process;
    }

    public String getProcessId() {
        return processId;
    }

    public Date getExecTime() {
        return execTime;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getResponse() {
        return response;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
