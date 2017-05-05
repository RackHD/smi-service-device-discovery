/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.common.DevicesIpsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
// @SpringApplicationConfiguration(Application.class)
// @WebIntegrationTest
public class DeviceDiscoveryControllerTest {

    // final String BASE_URL = "http://localhost:8445/wsman/discovery";
    //
    // public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    //
    // @Autowired
    // private IDiscoveryRepository discoveryRepository;
    //
    // @Autowired
    // IDiscoveryManager discoveryManager;
    //
    // // //Test RestTemplate to invoke the APIs.
    // private RestTemplate restTemplate = new TestRestTemplate();
    //
    // @BeforeClass
    // public static void initAll() {
    //
    // }
    //
    // @AfterClass
    // public static void cleanAll() throws Exception {
    // Thread.sleep(30000);
    //
    // }

    // @Test
    // public void startDiscoveryTest() throws Exception {
    // // discoveryManager.startDiscovery(buildDummyList());
    // Set<DiscoverDeviceRequest> set = new HashSet<DiscoverDeviceRequest>();
    // DiscoverDeviceRequest aa1 = new DiscoverDeviceRequest();
    // aa1.setCredential(new Credential("root", "calvin"));
    // aa1.setDeviceStartIp("100.68.123.1");
    // aa1.setDeviceEndIp("100.68.123.255");
    // aa1.setDeviceType(DeviceType.FXServer.name());
    // set.add(aa1);
    //
    // DiscoverDeviceRequest aa2 = new DiscoverDeviceRequest();
    // aa2.setCredential(new Credential("root", "calvin"));
    // aa2.setDeviceStartIp("100.68.124.1");
    // aa2.setDeviceEndIp("100.68.124.255");
    // aa2.setDeviceType(DeviceType.FXServer.name());
    // set.add(aa2);
    //
    // DiscoverDeviceRequest aa3 = new DiscoverDeviceRequest();
    // aa3.setCredential(new Credential("root", "calvin"));
    // aa3.setDeviceStartIp("100.68.125.1");
    // aa3.setDeviceEndIp("100.68.125.255");
    // aa3.setDeviceType(DeviceType.FXServer.name());
    // set.add(aa3);
    //
    // DiscoverDeviceRequest aa4 = new DiscoverDeviceRequest();
    // aa4.setCredential(new Credential("root", "calvin"));
    // aa4.setDeviceStartIp("100.68.126.1");
    // aa4.setDeviceEndIp("100.68.126.255");
    // aa4.setDeviceType(DeviceType.FXServer.name());
    // set.add(aa4);
    //
    // DiscoverDeviceRequest aa5 = new DiscoverDeviceRequest();
    // aa5.setCredential(new Credential("root", "calvin"));
    // aa5.setDeviceStartIp("172.31.62.1");
    // aa5.setDeviceEndIp("172.31.62.255");
    // aa5.setDeviceType(DeviceType.FXServer.name());
    // set.add(aa5);
    //
    // DiscoverIPRangeDeviceRequests a = new DiscoverIPRangeDeviceRequests(set);
    // a.setCredential(new Credential("root", "calvin"));
    // String json = OBJECT_MAPPER.writeValueAsString(a);
    // System.out.println("Request : " + json);
    // ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/discover", a, String.class);
    // String result = response.getBody();
    // System.out.println("Response : " + result);
    // Thread.sleep(60000);
    // }

    // @Test
    // public void getDiscoveryStatusRESTTest() throws Exception {
    // ResponseEntity<? extends ArrayList<ServerDiscoveryStatus>> response = restTemplate.getForEntity(BASE_URL + "/discoveryStatus", (Class<? extends
    // ArrayList<ServerDiscoveryStatus>>) ArrayList.class);
    // List<ServerDiscoveryStatus> json = response.getBody();
    // System.out.println("Status size - " + json.size());
    // System.out.println("Response : " + json);
    // }
    //
    //
    // @Test
    // public void getDiscoveredSummaryRESTTest() throws Exception {
    //
    // ResponseEntity<? extends ArrayList<HwSystem>> response = restTemplate.getForEntity(BASE_URL + "/deviceSummaries", (Class<? extends ArrayList<HwSystem>>) ArrayList.class);
    // List<HwSystem> json = response.getBody();
    // System.out.println("Summary size - " + json.size());
    // System.out.println("Response : " + json);
    // }

    // @Test
    // public void getDiscoveryStatusTest() throws Exception {
    // List<ServerDiscoveryStatus> statusList = discoveryRepository.getAllDiscoveryStatus();
    // System.out.println("Status size - " + statusList.size());
    // for (ServerDiscoveryStatus serverDiscoveryStatus : statusList) {
    // System.out.println("Device discovery status : - " + serverDiscoveryStatus.getIpAddress() + " :" + ReflectionToStringBuilder.toString(serverDiscoveryStatus, new
    // CustomRecursiveToStringStyle(99)));
    // }
    // }
    //
    //
    // @Test
    // public void getDiscoveredSummaryTest() throws Exception {
    // List<HwSystem> systemList = discoveryRepository.getAllSummires();
    // System.out.println("Summary size - " + systemList.size());
    // for (HwSystem system : systemList) {
    // System.out.println("Server Summary : - " + system.getId() + " :" + ReflectionToStringBuilder.toString(system, new CustomRecursiveToStringStyle(99)));
    // }
    // }
    //
    //
    // private static List<DiscoverDeviceInfo> buildDummyList() {
    // List<DiscoverDeviceInfo> discoveryInfos = new ArrayList<DiscoverDeviceInfo>();
    //
    // for (int i = 0; i < 255; i++) {
    // DiscoverDeviceInfo discoveryInfo = new DiscoverDeviceInfo();
    // discoveryInfo.setIpAddress("172.31.62." + i);
    // discoveryInfos.add(discoveryInfo);
    // }
    //
    // return discoveryInfos;
    // }

    @Test
    public void getJsonStringTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String[] ips = { "100.68.123.236", "100.68.123.136", "100.68.123.36" };
        DevicesIpsRequest request = new DevicesIpsRequest();
        request.setCredential(new Credential());
        request.setIps(ips);
        // Object to JSON in String
        String jsonInString = mapper.writeValueAsString(request);
        System.out.println(jsonInString);

    }

}
