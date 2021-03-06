### smi-service-device-discovery

Copyright © 2017 Dell Inc. or its subsidiaries. All Rights Reserved.

### Purpose

Given an IP Range and credentials, the service attemps to connect to each IP address to try and identify and retrieve basic summary information for all devices within the range. It is written primarily for finding Dell devices, but may identify a few others in the datacenter as well.  It is a stateless (12 factor app) that returns a JSON summary response.

This microservice can be used by itself, or as one piece of a larger discovery and inventory effort.   This service is used by RackHD as part of a Dell WSMAN discovery and inventory workflow (taskgraph). It is one of several Docker containerized micro-services used as part of the workflow.


### How to Use

A docker container for this service is available at: https://hub.docker.com/r/rackhd/device-discovery/

#### Startup
Standalone, with no configuration settings provided:
```
docker run --name device-discovery -p 0.0.0.0:46002:46002 -d rackhd/device-discovery:latest
```

The service can also start up to bootstrap its configuration from consul.  More information about registration with and using advanced configuration settings provided by a Consul K/V store can be found in the online help.


#### API Definitions

A swagger UI is provided by the microservice at http://<ip>:46002/swagger-ui.html

#### Synchronous CURL example with one network range payload ###
The micro-service REST endpoints can be invoked via CURL or via another application.

Example:
```
curl -X POST -H 'Content-Type: application/json' -d '{"credential":{"user": "root","password": "calvin"},"discoverIpRangeDeviceRequests":[{"deviceType": null,"deviceStartIp": "100.68.123.1","deviceEndIp": "100.68.123.254","credential": null}]}' http://<ip>:46002/api/1.0/discover/range
```

#### Example payloads
The payload allows for global and local (per range) credentials and device types.  Global values are always overwritten by local values.


###### Example Payload 1.
Use global credentials (override all credentials in application yml for all devices)
~~~
/api/1.0/discover/range

{
   "credential":{
      "user":"root",
      "password":"calvin"
   },
   "discoverIpRangeDeviceRequests":[
      {
         "deviceStartIp":"100.68.124.6",
         "deviceEndIp":"100.68.124.57",
      }
   ]
}

/api/1.0/discover/ips:

{
 "credential": {
    "password": "calvin1",
    "userName": "root1"
  },
 "ips": [
    "100.68.124.37","100.68.124.43"
  ]
}
~~~

###### Example Payload 2.
Use Credential specific to device type (Override credential from application YML specific to devices).  If global credentials are supplied (as in example 1 above), the specific device credentials will be used instead for that device type.
~~~
/api/1.0/discover/range

{
   "discoverIpRangeDeviceRequests":[
      {
         "deviceType":["SERVER"],
         "deviceStartIp":"100.68.124.0",
         "deviceEndIp":"100.68.124.255",
         "credential":{
			"userName":"abc0",
			"password":"xyz0"
		 }
      },
      {
         "deviceType":["CHASSIS"],
         "deviceStartIp":"100.68.12.0",
         "deviceEndIp":"100.68.12.255",
         "credential":{
			"userName":"abc1",
			"password":"xyz1"
		 }
      },
      {
         "deviceType":["IOM"],
         "deviceStartIp":"100.68.123.0",
         "deviceEndIp":"100.68.123.255",
         "credential":{
			"userName":"abc2",
			"password":"xyz2"
		 }
      },
      {
         "deviceType":["SWITCH"],
         "deviceStartIp":"100.68.13.0",
         "deviceEndIp":"100.68.13.255",
         "credential":{
			"userName":"abc3",
			"password":"xyz3"
		 }
      },
      {
         "deviceType":["VM"],
         "deviceStartIp":"100.68.126.0",
         "deviceEndIp":"100.68.126.255",
         "credential":{
			"userName":"abc4",
			"password":"xyz4"
		 }
      },
      {
         "deviceType":["STORAGE"],
         "deviceStartIp":"100.68.14.0",
         "deviceEndIp":"100.68.14.255",
         "credential":{
			"userName":"abc5",
			"password":"xyz5"
		 }
      }
   ]
}

/api/1.0/discover/ips: No support
~~~

###### Example Payload 3.
Use default device credentials (configured in application.yml).  This option is for when you want to pull credentials from a supplied configuration at service startup, to prevent the consuming application from needing to be aware of them.

```

/api/1.0/discover/range

{
   "discoverIpRangeDeviceRequests":
   [
      {
         "deviceType":["SERVER"],
         "deviceStartIp":"100.68.124.6",
         "deviceEndIp":"100.68.124.57",
      },
      {
         "deviceType":["CHASSIS"],
         "deviceStartIp":"100.68.123.6",
         "deviceEndIp":"100.68.123.57",
      }
   ]
}

/api/1.0/discover/ips:

{
 "ips": [
    "100.68.124.37","100.68.124.43"
  ]
}

OR 

{
 "deviceType":["SERVER","CHASSIS"],
 "ips": [
    "100.68.124.37","100.68.124.43"
  ]
}

```

##### Example Payload 4.
The example payload below will attempt to discover servers and chassis in the ip range 100.68.124.11 - 57, using the global credentials "root" and "calvin".  It will also attempt to discover switches in the ip range of 100.68.124.6 - 10, using the range specific credentials "abc0" and "xyz0"

~~~

/api/1.0/discover/range
{
   "credential":{
      "user": "root",
      "password": "calvin"
   },
   "deviceType": ["SERVER", "CHASSIS"],
   "discoverIpRangeDeviceRequests":[
        {
            "deviceStartIp": "100.68.124.6",
            "deviceEndIp": "100.68.124.10",
            "credential":{
                "userName": "abc0",
                "password": "xyz0"
            },
            "deviceType": ["SWITCH"],
        },
        {
            "deviceStartIp": "100.68.124.11",
            "deviceEndIp": "100.68.124.57"
        }
   ]
}

/api/1.0/discover/ips: No support
~~~
- ##### _supported devices group are {SERVER,CHASSIS and STORAGE (Complellent) - Discovery and brief summary;}
- VM and SWITCH (Only discovery but NO Summary)
Supported device types:

~~~
SERVER: 
   IDRAC7, 
   IDRAC8,

CHASSIS:
    CMC,
    CMC_FX2,
    CSERVER,
    VRTX
	
SWITCH:
    FORCE10_S4810,
    FORCE10_S5000,
    FORCE10_S6000,
    FORCE10_S4048,
    FORCE10_S55
    BROCADE,
    POWERCONNECT,
    POWERCONNECT_N3000,
    POWERCONNECT_N4000,
    CISCONEXUS

STORAGE:
    COMPELLENT,

VM:
	VCENTER
~~~
---

#### Licensing
Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Source code for this microservice is available in repositories at https://github.com/RackHD.  

The microservice makes use of dependent Jar libraries that may be covered by other licenses. In order to comply with the requirements of applicable licenses, the source for dependent libraries used by this microservice is available for download at:  https://bintray.com/rackhd/binary/download_file?file_path=smi-service-device-discovery-dependency-sources-devel.zip

Additionally the binary and source jars for all dependent libraries are available for download on Maven Central.

RackHD is a Trademark of Dell EMC


### Support
Slack Channel: codecommunity.slack.com
