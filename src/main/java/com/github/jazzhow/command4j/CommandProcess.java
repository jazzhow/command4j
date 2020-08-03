package com.github.jazzhow.command4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.*;

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

    private static ExecutorService executorService;

    static {
        executorService = new ThreadPoolExecutor(3, 5, 10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2048),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName(r.getClass().getName());
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


    protected CommandProcess(String processId, Process process, Date execTime, String command, CommandManager commandManager) {
        this.processId = processId;
        this.process = process;
        this.execTime = execTime;
        this.command = command;
        final BufferedReader errBfReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final BufferedReader stdBfReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        executorService.execute(() -> executeBfReader(errBfReader));
        executorService.execute(() -> executeBfReader(stdBfReader));
        executorService.execute(() -> executeProcessWaitFor(processId, process, commandManager));
        executorService.shutdown();
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

    private void executeBfReader(BufferedReader errBfReader) {
        String line;
        try {
            while (!ioStop && (line = errBfReader.readLine()) != null) {
                LOGGER.debug(line);
            }
        } catch (IOException ignored) {
        } finally {
            try {
                errBfReader.close();
            } catch (IOException ignored) {
            }
        }
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
}
