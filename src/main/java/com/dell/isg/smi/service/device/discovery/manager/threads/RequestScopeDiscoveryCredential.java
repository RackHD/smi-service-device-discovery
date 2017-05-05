/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.manager.threads;

import java.util.concurrent.ConcurrentHashMap;

import com.dell.isg.smi.commons.model.common.Credential;

public class RequestScopeDiscoveryCredential {

    ConcurrentHashMap<String, Credential> credentialMap = new ConcurrentHashMap<String, Credential>();


    public ConcurrentHashMap<String, Credential> getCredentialMap() {
        return credentialMap;
    }


    public void setCredentialMap(ConcurrentHashMap<String, Credential> credentialMap) {
        this.credentialMap = credentialMap;
    }


    public void add(String device, Credential credential) {
        credentialMap.put(device, credential);
    }


    public void replace(String device, Credential credential) {
        if (credentialMap.containsKey(device)) {
            credentialMap.remove(device, credential);
            credentialMap.put(device, credential);
        } else {
            credentialMap.put(device, credential);
        }
    }


    public Credential getCredential(String device) {
        return credentialMap.get(device);
    }


    public void clear() {
        credentialMap.clear();
    }

}
