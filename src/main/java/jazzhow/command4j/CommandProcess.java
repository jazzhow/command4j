package jazzhow.command4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class CommandProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcess.class);
    private volatile boolean normalExit = false;
    private Process process;
    private String processId;
    private Thread errInput;
    private Thread stdInput;

    public CommandProcess(String processId, Process process, ConcurrentHashMap<String, CommandProcess> processMap) {
        this.processId = processId;
        this.process = process;
        BufferedReader errBfReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        Thread errBfReaderThread = new Thread(() -> {
            try {
                String line;
                while (true) {
                    try {
                        if ((line = errBfReader.readLine()) == null) {
                            break;
                        }
                        LOGGER.debug(line);
                    } catch (IOException e) {
                        if (!process.isAlive()) {
                            return;
                        }
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            } finally {
                if (errBfReader != null) {
                    try {
                        errBfReader.close();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }

                if (!process.isAlive()) {
                    processMap.remove(processId);
                    int i = process.exitValue();
                    if (normalExit) {
                        LOGGER.info("程序" + processId + "运行完毕，返回代码: " + i);
                    } else {
                        LOGGER.warn("程序" + processId + "被外界环境关闭，返回代码: " + i);
                    }
                }
            }
        });
        BufferedReader stdBfReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Thread stdBfReaderThread = new Thread(() -> {
            try {
                String line;
                while (true) {
                    try {
                        if ((line = stdBfReader.readLine()) == null) {
                            break;
                        }
                        LOGGER.debug(line);
                    } catch (IOException e) {
                        if (!process.isAlive()) {
                            return;
                        }
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            } finally {
                if (stdBfReader != null) {
                    try {
                        stdBfReader.close();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });
        errBfReaderThread.setName("errBfReaderThread-" + processId);
        stdBfReaderThread.setName("stdBfReaderThread-" + processId);
        errBfReaderThread.start();
        stdBfReaderThread.start();
    }

    public void setNormalExit(boolean normalExit) {
        this.normalExit = normalExit;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Thread getErrInput() {
        return errInput;
    }

    public void setErrInput(Thread errInput) {
        this.errInput = errInput;
    }

    public Thread getStdInput() {
        return stdInput;
    }

    public void setStdInput(Thread stdInput) {
        this.stdInput = stdInput;
    }

}
