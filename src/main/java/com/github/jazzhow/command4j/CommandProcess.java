package com.github.jazzhow.command4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

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

    protected CommandProcess(String processId, Process process, Date execTime, String command, CommandManager commandManager) {
        this.processId = processId;
        this.process = process;
        this.execTime = execTime;
        this.command = command;
        final BufferedReader errBfReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        Thread errBfReaderThread = new Thread(() -> {
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
        });
        final BufferedReader stdBfReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Thread stdBfReaderThread = new Thread(() -> {
            String line;
            try {
                while (!ioStop && (line = stdBfReader.readLine()) != null) {
                    LOGGER.debug(line);
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    stdBfReader.close();
                } catch (IOException ignored) {
                }
            }
        });
        Thread processWaitForThread = new Thread(() -> {
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
        });
        processWaitForThread.setName("processWaitForThread-" + processId);
        errBfReaderThread.setName("errBfReaderThread-" + processId);
        stdBfReaderThread.setName("stdBfReaderThread-" + processId);
        processWaitForThread.start();
        errBfReaderThread.start();
        stdBfReaderThread.start();
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
