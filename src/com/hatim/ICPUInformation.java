package com.hatim;

import com.hatim.models.LSCPUInformation;
import com.hatim.models.ProcCPUInformation;

import java.util.HashMap;

/**
 * Created by hatim on 1/10/16.
 */
public interface ICPUInformation {

    void readLSCPU(LSCPUInformation cpuInformation);
    ProcCPUInformation readProc_cpuinfo(String procCPUinfoFilePath);
}
