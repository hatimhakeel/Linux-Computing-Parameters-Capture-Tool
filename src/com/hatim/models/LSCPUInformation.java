package com.hatim.models;

import java.util.List;

/**
 * Created by hatim on 1/10/16.
 */
public class LSCPUInformation {
    public String architecture;
    public List<String> CPUOpModes;
    public String byteOrder;
    public int processors;
    public int sockets;
    public int coresPerSocket;
    public int threadsPerCore;
    public String vendorID;
    public int CPUSpeed;
    public String CPUSpeedUnits;
    public float bogoMIPS;
    public String virtualization;
    public int L1dCache;
    public String L1dCacheUnits;
    public int L1iCache;
    public String L1iCacheUnits;
    public int L2Cache;
    public String L2CacheUnits;
    public int L3Cache;
    public String L3CacheUnits;
}
