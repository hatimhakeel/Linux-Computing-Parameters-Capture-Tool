package com.hatim;

import com.hatim.models.ProcCPUInformation;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * This class gathers information about several parameters of a Linux node
 * & writes the output to a file called "LinuxNodeParameters" in the users current
 * working directory's (project directory) output folder.
 *
 * @author Hatim
 */
public class CPU {

    private String outputFilePath;
    private String procCPUinfoFilePath;
    private String procStatFilePath;

//    String CPUOpModes[] = null;
//    String byteOrder;
//    int numberOfSockets;
//    int numberOfCoresPerSoket;
//    int numberOfThreadsPerCore;
//    int L1dCache, L1iCache, L2Cache, L3Cache;
//    String L1dCacheUnits, L1iCacheUnits, L2CacheUnits, L3CacheUnits;
//    String vendor;
//    String modelName;
//    String cpuFrequencyUnits;
    Double freeCpu = 0D;
    float CPULoad_1Min = 0f, CPULoad_5Min = 0f, CPULoad_15Min = 0f;
    String cacheUnits;
    String cacheSize;
    int numRunningProcesses;

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setProcCPUinfoFilePath(String procCPUinfoFilePath) {
        this.procCPUinfoFilePath = procCPUinfoFilePath;
    }
    public String getProcCPUinfoFilePath() {
        return procCPUinfoFilePath;
    }

    public void setProcStatFilePath(String procStatFilePath) {
        this.procStatFilePath = procStatFilePath;
    }
    public String getProcStatFilePath() {
        return procStatFilePath;
    }

    /**
     * Writes the collected parameter values to a file.
     */
    public void writeToFile(ProcCPUInformation cpu) {

        BufferedWriter bufferedWriter;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(getOutputFilePath()));
            try {
                bufferedWriter.write("#####################################################################\n");
                bufferedWriter.write("######                                                         ######\n");
                bufferedWriter.write("######       Values of several parameters of a linux node      ######\n");
                bufferedWriter.write("######       --------------------------------------------      ######\n");
                bufferedWriter.newLine();
                bufferedWriter.newLine();

                int numCPUCores = 0;
                for(int i = 0; i < cpu.processors.length; i++) {
                    if(cpu.cpuSpeeds[i] == 0)
                        break;
                    if (i == 0) {
                        bufferedWriter.write(" 01." + (i+1) + ".  CPU Speed " + "(processor-0" + i + ") : " + cpu.cpuSpeeds[i] + " " +
                            cpu.cpuFrequencyUnits + "\n");
                    } else {
                        bufferedWriter.write("    " + (i+1) + ".  CPU Speed " + "(processor-0" + i + ") : " + cpu.cpuSpeeds[i] + " " +
                                cpu.cpuFrequencyUnits + "\n");
                    }
                    numCPUCores = i+1;
                }
                bufferedWriter.write(" 02.    No of CPU cores : " + numCPUCores + "\n");
                bufferedWriter.write(" 03.    CPU architecture : " + System.getProperty("os.arch") + "\n");
                for (int i = 0; i < cpu.flags.length; i++) {
                    if (cpu.flags[i] == null)
                        break;
                    if (i == 0) {
                        bufferedWriter.write(" 04." + (i+1) + ".  CPU options " + "(processor-0" + i + ")" + " : ");
                    } else {
                        bufferedWriter.write("    " + (i+1) + ".  CPU options " + "(processor-0" + i + ")" + " : ");
                    }
                    for (int j = 2; j < cpu.flags[i].length; j++)
                        bufferedWriter.write(cpu.flags[i][j] + " ");
                    bufferedWriter.write("\n");
                }
                bufferedWriter.write(" 05.    CPU vendor : " + cpu.vendor + "\n");
                bufferedWriter.write(" 06.    CPU model name : " + cpu.modelName + "\n");

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                bufferedWriter.write(" 07.    Free CPU : " +
                        decimalFormat.format((CPUInfoUtil.getFreeCPU(getProcStatFilePath()) * 100)) + " %" + "\n");

                HashMap<String, Float> cpuLoads = CPUInfoUtil.getCPULoads();
                if (cpuLoads != null) {
                    bufferedWriter.write(" 08.1.  CPU Load (1 min)  : " + cpuLoads.get("CPULoad_1Min") + "\n");
                    bufferedWriter.write("    2.  CPU Load (5 min)  : " + cpuLoads.get("CPULoad_5Min") + "\n");
                    bufferedWriter.write("    3.  CPU Load (15 min) : " + cpuLoads.get("CPULoad_15Min") + "\n");
                }

                //bufferedWriter.write(" 08.    Cache Size : " + cacheSize + " " + cacheUnits + "\n");

                HashMap<String, Integer> memoryUsage = CPUInfoUtil.getMemoryUsage();
                if (memoryUsage != null) {
                    bufferedWriter.write(" 09.    Total Memory : " + memoryUsage.get("TotalMemory")
                            + " MB" + System.lineSeparator());
                    bufferedWriter.write(" 10.    Free Memory  : " + memoryUsage.get("FreeMemory")
                            + " MB" + System.lineSeparator());
                }

                //Put in a method.
                File file = new File("/");
                bufferedWriter.write(" 11.    Total disk space of root partition : " + file.getTotalSpace()/1024/1024/1024
                        + " GB" + System.lineSeparator());
                bufferedWriter.write(" 12.    Free disk space of root partition : " + file.getFreeSpace()/1024/1024/1024
                        + " GB" + System.lineSeparator());

                bufferedWriter.write(" 13.    Operating System Type : " + System.getProperty("os.name")
                        + System.lineSeparator());
                bufferedWriter.write(" 14.    Kernel Version : " + System.getProperty("os.version")
                        + System.lineSeparator());

                bufferedWriter.write(" 15.    Number of Running Processes : "
                        + CPUInfoUtil.getActiveProcessesCount() + "\n");

            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {

        ProcCPUInformation procCPUInformation = null;

        CPU cpu = new CPU();
        String currentDirectory = System.getProperty("user.dir");
        cpu.setOutputFilePath(currentDirectory + "/output/LinuxNodeParameters");
        cpu.setProcCPUinfoFilePath("/proc/cpuinfo");
        cpu.setProcStatFilePath("/proc/stat");

        CPUInfoUtil cpuInfoUtil = new CPUInfoUtil();
        procCPUInformation = cpuInfoUtil.readProc_cpuinfo(cpu.getProcCPUinfoFilePath());

        cpu.writeToFile(procCPUInformation);
    }
}

