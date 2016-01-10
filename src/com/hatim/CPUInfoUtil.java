package com.hatim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import com.hatim.models.LSCPUInformation;
import com.hatim.models.ProcCPUInformation;

import java.util.LinkedList;

/**
 * A utility class with methods to gather various computing device parameters
 *
 * Created by hatim on 1/1/16.
 */
public class CPUInfoUtil implements ICPUInformation {
    final static int KiBFACTOR = 1024;
    final static int LOOP_UPPERBOUND = 5;

    enum ProcessorCountTool {
        NPROC,
        PROC_STAT,
        LSCPU
    }

    enum CPUData {
        ARCHITECTURE,
        CPU_OP_MODE_S_,
        BYTE_ORDER,
        CPU_S_,
        SOCKET_S_,
        CORE_S__PER_SOCKET,
        THREAD_S__PER_CORE,
        VENDOR_ID,
        CPU_MHZ,
        CPU_GHZ,
        BOGOMIPS,
        VIRTUALIZATION,
        L1D_CACHE,
        L1I_CACHE,
        L2_CACHE,
        L3_LACHE
    }

    /**
     * Gets cpu loads using the <>uptime</> command.
     *
     * @return a HashMap<String, Float> containing the 3 CPU loads.
     */
    public static HashMap<String, Float> getCPULoads() {
        Process process = null;
        BufferedReader bufferedReader = null;
        String line = null;
        String tempStore[] = null;
        HashMap<String, Float> cpuLoads = new HashMap<>();
        float CPULoad_1Min, CPULoad_5Min, CPULoad_15Min;

        try {
            try {
                process = Runtime.getRuntime().exec("uptime");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                line = bufferedReader.readLine();
                tempStore = line.split("\\s+");
                for (int i = 0; i < tempStore.length; i++) {
                    if(tempStore[i].equalsIgnoreCase("load") && tempStore[i+1].equalsIgnoreCase("average:")) {
                        CPULoad_1Min = Float.parseFloat(tempStore[i+2].replace(",", ""));
                        CPULoad_5Min = Float.parseFloat(tempStore[i+3].replace(",", ""));
                        CPULoad_15Min = Float.parseFloat(tempStore[i+4]);

                        cpuLoads.put("CPULoad_1Min", CPULoad_1Min);
                        cpuLoads.put("CPULoad_5Min", CPULoad_5Min);
                        cpuLoads.put("CPULoad_15Min", CPULoad_15Min);
                    }
                }
            } catch (IOException ioExp) {
                ioExp.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }

        return cpuLoads;
    }

    /**
     * Gets memory usage using the <>top -b -n 1</> command.
     *
     * @return a HashMap<String, Integer> containing Total and Free memory.
     */
    public static HashMap<String, Integer> getMemoryUsage() {
        BufferedReader bufferedReader = null;
        HashMap<String, Integer> memoryUsage = new HashMap<>();
        int totalMemory, freeMemory;

        try {
            try {
                Process process = Runtime.getRuntime().exec("top -b -n 1");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = bufferedReader.readLine();
                while (line != null && !line.startsWith("Mem") && !line.startsWith("KiB Mem")) {
                    line = bufferedReader.readLine();
                }
                String tempStore[] = line.split("\\s+");
                if (line.startsWith("Mem")) {
                    totalMemory = new Integer(tempStore[1])/KiBFACTOR;
                    freeMemory = new Integer(tempStore[5])/KiBFACTOR;

                    memoryUsage.put("TotalMemory", totalMemory);
                    memoryUsage.put("FreeMemory", freeMemory);
                }
                if (line.startsWith("KiB Mem")) {
                    totalMemory = new Integer(tempStore[2])/KiBFACTOR;
                    freeMemory = new Integer(tempStore[6])/KiBFACTOR;

                    memoryUsage.put("TotalMemory", totalMemory);
                    memoryUsage.put("FreeMemory", freeMemory);
                }
            } catch (IOException ioExp) {
                ioExp.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }

        return memoryUsage;
    }

    /**
     * Parses the lscpu command output and populates a CPUInformation object
     *
     * @params a CPUInformation {@code Object}
     */
    public void readLSCPU(LSCPUInformation cpuInformation) {
        BufferedReader bufferedReader = null;
        String line, input, value;
        String tempStore[];
        String cacheInformation[];

        try {
            try {
                Process process = Runtime.getRuntime().exec("lscpu");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                line = bufferedReader.readLine();
                while (line != null) {
                    tempStore = line.split(":");
                    value = tempStore[1].trim();
                    input = formatStringsForEnum(tempStore[0].trim());
                    CPUData cpuData = CPUData.valueOf(input);

                    switch (cpuData) {
                        case ARCHITECTURE:
                            cpuInformation.architecture = value;
                            break;
                        case CPU_OP_MODE_S_:
                            cpuInformation.CPUOpModes = new LinkedList<>();
                            String opModes[] = value.split(",");
                            cpuInformation.CPUOpModes.add(opModes[0]);
                            cpuInformation.CPUOpModes.add(opModes[1]);
                            break;
                        case BYTE_ORDER:
                            cpuInformation.byteOrder = value;
                            break;
                        case CPU_S_:
                            cpuInformation.processors = new Integer(value);
                            break;
                        case SOCKET_S_:
                            cpuInformation.sockets = new Integer(value);
                            break;
                        case CORE_S__PER_SOCKET:
                            cpuInformation.coresPerSocket = new Integer(value);
                            break;
                        case THREAD_S__PER_CORE:
                            cpuInformation.threadsPerCore = new Integer(value);
                            break;
                        case VENDOR_ID:
                            cpuInformation.vendorID = value;
                            break;
                        case CPU_MHZ:
                        case CPU_GHZ:
                            cpuInformation.CPUSpeed = new Integer(value);
                            cpuInformation.CPUSpeedUnits = tempStore[0].trim().split("\\s")[1];
                            break;
                        case BOGOMIPS:
                            cpuInformation.bogoMIPS = new Integer(value);
                            break;
                        case VIRTUALIZATION:
                            cpuInformation.virtualization = value;
                            break;
                        case L1D_CACHE:
                            cacheInformation = value.split("\\w");
                            cpuInformation.L1dCache = new Integer(cacheInformation[0]);
                            cpuInformation.L1dCacheUnits = cacheInformation[1];
                            break;
                        case L1I_CACHE:
                            cacheInformation = value.split("\\w");
                            cpuInformation.L1iCache = new Integer(cacheInformation[0]);
                            cpuInformation.L1iCacheUnits = cacheInformation[1];
                            break;
                        case L2_CACHE:
                            cacheInformation = value.split("\\w");
                            cpuInformation.L2Cache = new Integer(cacheInformation[0]);
                            cpuInformation.L2CacheUnits = cacheInformation[1];
                            break;
                        case L3_LACHE:
                            cacheInformation = value.split("\\w");
                            cpuInformation.L3Cache = new Integer(cacheInformation[0]);
                            cpuInformation.L3CacheUnits = cacheInformation[1];
                            break;
                    }
                }

            } catch (IOException ioExp) {
                ioExp.printStackTrace();
            }
            finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
    }

    private int[] initProcessors(int numberOfProcessors) throws Exception {
        int processors[];
        if (numberOfProcessors != 0) {
            processors = new int[numberOfProcessors];
        } else {
            throw new Exception("initial array length should be > 0");
        }
        return processors;
    }

    private float[] initCPUSpeeds(int numberOfProcessors) throws Exception {
        float cpuSpeeds[];
        if (numberOfProcessors != 0) {
            cpuSpeeds = new float[numberOfProcessors];
        } else {
            throw new Exception("initial array length should be > 0");
        }
        return cpuSpeeds;
    }

    private String[][] initFlags(int numberOfProcessors) throws Exception {
        String flags[][];
        if (numberOfProcessors != 0) {
            flags = new String[numberOfProcessors][];
        } else {
            throw new Exception("initial array length should be > 0");
        }
        return flags;
    }

    /**
     * Reads /proc/cpuinfo file to collect information regarding,
     * 1. Vendor    2. CPU frequency units    3. CPU speed     4. CPU flags
     *
     * @params {@code String} /proc/cpuinfo file path, {@code Object} container for /proc/cpuinfo
     * properties mentioned above
     */
    public ProcCPUInformation readProc_cpuinfo(String procCPUinfoFilePath) {

        ProcCPUInformation cpuInformation = new ProcCPUInformation();
        int numberOfProcessors = getProcessorCount(ProcessorCountTool.PROC_STAT);

        try {
            cpuInformation.processors = initProcessors(numberOfProcessors);
            cpuInformation.cpuSpeeds = initCPUSpeeds(numberOfProcessors);
            cpuInformation.flags = initFlags(numberOfProcessors);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        BufferedReader bufferedReader = null;
        String tempStore[];
        String line;
        int processorIndex = 0;
        int cpuSpeedIndex = 0;
        int cpuFlagsIndex = 0;

        try {
            try {
                bufferedReader = new BufferedReader(new FileReader(procCPUinfoFilePath));
                line = bufferedReader.readLine();
                while (line != null) {
                    if(line.startsWith("processor")) {
                        tempStore = line.split("\\s+");
                        if (tempStore[0].equals("processor")) {
                            cpuInformation.processors[processorIndex++] = new Integer(tempStore[2]);
                        }
                        do {
                            line = bufferedReader.readLine();
                            if(line.startsWith("vendor_id")) {
                                tempStore = line.split("\\s+");
                                cpuInformation.vendor = tempStore[2];
                            }
                            if (line.startsWith("model name")) {
                                tempStore = line.split(":");
                                cpuInformation.modelName = tempStore[1].trim();
                            }
                            if(line.startsWith("cpu")) {
                                tempStore = line.split("\\s+");
                                if(tempStore[1].equals("KHz") || tempStore[1].equals("MHz") ||
                                        tempStore[1].equals("GHz")) {
                                    cpuInformation.cpuFrequencyUnits = tempStore[1];
                                    cpuInformation.cpuSpeeds[cpuSpeedIndex++] = new Float(tempStore[3]);
                                }
                            }
                            if(line.startsWith("flags")) {
                                tempStore = line.split("\\s+");
                                cpuInformation.flags[cpuFlagsIndex++] = tempStore;
                            }
                            if(line.startsWith("bogomips")) {
                                tempStore = line.split("\\s+");
                                cpuInformation.bogomips = new Float(tempStore[2]);
                            }
                            if(line.startsWith("cache_alignment")) {
                                tempStore = line.split("\\s+");
                                cpuInformation.cacheAlignment = new Integer(tempStore[2]);
                            }
                        } while (!line.equals(""));
                        line = bufferedReader.readLine();
                    }
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cpuInformation;
    }

    /**
     * Reads /proc/stat file to collect information regarding free CPU.
     *
     * @params {@code String} /proc/stat file path
     * @return {@code Double} free cpu
     */
    private static Double readProc_stat(String procStatFilePath) {

        BufferedReader predecessorBufferedReader = null;
        BufferedReader successorBufferedReader = null;
        String predecessorTimeSnapshot[] = null;
        String successorTimeSnapshot[] = null;
        int count = 0, loopUpperBound = LOOP_UPPERBOUND;
        Double totalCPUTime = 0D, tempFreeCPU = 0D, totalFreeCPU = 0D, freeCpu = 0D;
        String line;

        try {
            /**
             * Free CPU is calculated by subtracting two time slices taken within a 0.5 seconds time gap from
             * the /proc/stat file 5 times & then calculating the average.
             */
            while (count < loopUpperBound) {
                count++;
                predecessorBufferedReader = new BufferedReader(new FileReader(procStatFilePath));

                try {
                    line = predecessorBufferedReader.readLine();
                    predecessorTimeSnapshot = line.split("\\s+");
                } finally {
                    predecessorBufferedReader.close();
                }

                long start = System.currentTimeMillis();
                long end = start + 500;     //halt program for .5 seconds before reading CPU times from stat.
                while(System.currentTimeMillis() < end) {}

                successorBufferedReader = new BufferedReader(new FileReader(procStatFilePath));

                try {
                    line = successorBufferedReader.readLine();
                    successorTimeSnapshot = line.split("\\s+");
                } finally {
                    successorBufferedReader.close();
                }

                Double diffOfTimeSnapshot[] = new Double[successorTimeSnapshot.length - 1];

                if (successorTimeSnapshot[0].equalsIgnoreCase("cpu") && predecessorTimeSnapshot[0].equalsIgnoreCase("cpu")) {
                    for (int i = 1; i < successorTimeSnapshot.length; i++)
                        diffOfTimeSnapshot[i-1] = Double.parseDouble(successorTimeSnapshot[i]) - Double.parseDouble(predecessorTimeSnapshot[i]);
                    for (int i = 0; i < diffOfTimeSnapshot.length; i++)
                        totalCPUTime += diffOfTimeSnapshot[i];
                    tempFreeCPU = Double.valueOf(diffOfTimeSnapshot[3])/totalCPUTime;
                }
                totalFreeCPU += tempFreeCPU;
                tempFreeCPU = 0D;
                totalCPUTime = 0D;
            }
            freeCpu = totalFreeCPU / loopUpperBound;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return freeCpu;
    }

    public static Double getFreeCPU(String procStatFilePath) {
        Double freeCPU = readProc_stat(procStatFilePath);
        return freeCPU;
    }

    /**
     * Gets the active processes count using the <>ps -ef</> command.
     *
     * @return an int denoting the active processes count.
     */
    public static int getActiveProcessesCount() {
        Process process;
        BufferedReader bufferedReader = null;
        String line;
        int processCounter = -1;
        int numberOfActiveProcesses = 0;

        try {
            try {
                process = Runtime.getRuntime().exec("ps -ef");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                line = bufferedReader.readLine();

                while(line != null) {
                    processCounter++;
                    line = bufferedReader.readLine();
                }
                numberOfActiveProcesses = processCounter;
            } catch (IOException ioExp) {
                ioExp.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }

        return numberOfActiveProcesses;
    }

    /**
     * Gets the processor count using @param
     * @params  a {@code String} containing one of several possible commands that gives
     *          the processor count as an output
     * @return  the {@code int} number of processors
     */
    private static int getProcessorCount(String command) {
        BufferedReader bufferedReader = null;
        Process process = null;
        String line = null;
        int numberOfProcessors = 0;
        String lineStore[] = null;

        try {
            try {
                process = Runtime.getRuntime().exec(command);
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                line = bufferedReader.readLine();
                if (!line.startsWith("Architecture")) {
                    numberOfProcessors = new Integer(line.trim());
                } else {
                    do {
                        line = bufferedReader.readLine();
                    } while (!line.startsWith("CPU(s)"));
                    lineStore = line.split("\\s+");
                    numberOfProcessors = new Integer(lineStore[1].trim());
                }
            } catch (IOException ioExp) {
                ioExp.printStackTrace();
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return numberOfProcessors;
    }

    /**
     * Formats strings to apply as enum values.
     * e.g. CPU op-mode(s) -> CPU_OP_MODE_S_
     * @params  a {@code String} to be converted into an enum value
     * @return  formatted {@code String}
     */
    private String formatStringsForEnum(String value) {

        String formattedValue = value.replaceFirst("\\s|-|(|)", "_").toUpperCase();

        return formattedValue;
    }

    /**
     * This method is exposed by the interface. It internally calls the
     * getProcessorCount({@code String} command) which executes the command
     * in a separate process and returns the result.
     *
     * @param       processorCountTool      an enum indicating the tool to use to
     *                                      get the number of processing elements.
     * @return      an {@code int} indicating the number of processors.
     */
    public static int getProcessorCount(ProcessorCountTool processorCountTool) {
        int numberOfProcessors = 0;

        switch (processorCountTool) {
            case NPROC:
                numberOfProcessors = getProcessorCount("nproc");
                break;
            case LSCPU:
                numberOfProcessors = getProcessorCount("lscpu");
                break;
            case PROC_STAT:
                numberOfProcessors = getProcessorCount("grep -c processor /proc/cpuinfo");
                break;
        }
        return numberOfProcessors;
    }
}
