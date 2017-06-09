/**
 * Copyright © 2017 DELL Inc. or its subsidiaries.  All Rights Reserved.
 */
package com.dell.isg.smi.service.device.discovery.utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dell.isg.smi.commons.model.common.Credential;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveredDeviceInfo;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceIdentifierEnum;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceStatus;
import com.dell.isg.smi.commons.model.device.discovery.DiscoveryDeviceTypeEnum;
import com.dell.isg.smi.commons.model.device.discovery.config.DeviceType;
import com.dell.isg.smi.commons.model.device.discovery.config.Identifier;
import com.dell.isg.smi.commons.utilities.CustomRecursiveToStringStyle;
import com.dell.isg.smi.service.device.discovery.manager.threads.RequestScopeDiscoveryCredential;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;

@Component
public class DiscoverDeviceTypeUtil {

	private static final Logger logger = LoggerFactory.getLogger(DiscoverDeviceTypeUtil.class.getName());
	private static final String EXIT_COMMAND = "exit\n";
	private static final String NEWLINE_COMMAND = "\n";

	private static RequestScopeDiscoveryCredential requestScopeDiscoveryCredential;

	public static void processHttps(DiscoveredDeviceInfo discoverDeviceInfo, String command,
			List<DeviceType> deviceTypeList) {
		String ipAddress = discoverDeviceInfo.getIpAddress();
		String deviceUri = String.format(command, ipAddress);
		String response = getHttpResponse(deviceUri);
		if (response == null) {
			discoverDeviceInfo.setDeviceType(DiscoveryDeviceTypeEnum.UNKNOWN.value());
			discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.UNKNOWN.name());
			return;
		}
		boolean isDeviceIdentified = false;		
		for (DeviceType deviceType : deviceTypeList) {
			if ((Boolean.TRUE == isDeviceIdentified)){
				break;
			}
			switch (DiscoveryDeviceIdentifierEnum.valueOf(deviceType.getIdentifyBy())) {
			case REGEX:
				isDeviceIdentified = doesResponseMatchesIdentifierPattern(response, deviceType.getIdentifier());
				break;
			case REGULAR:
				isDeviceIdentified = doesResponseContainIdentifiers(response, deviceType.getIdentifier());
				break;
			}
			if (isDeviceIdentified){
				discoverDeviceInfo.setDeviceType(deviceType.getName());
				discoverDeviceInfo.setStatus(DiscoveryDeviceStatus.DEVICE_IDENTFIED.name());		        
			}
		}
		

	}

	public static void processTcp(DiscoveredDeviceInfo discoverDeviceInfo, String command,
			List<DeviceType> deviceTypeList) {
		// TODO Auto-generated method stub
	}

	public static void processTftp(DiscoveredDeviceInfo discoverDeviceInfo, String command,
			List<DeviceType> deviceTypeList) {
		// TODO Auto-generated method stub
	}

	public static void processSsh(DiscoveredDeviceInfo discoverDeviceInfo, String command,
			List<DeviceType> deviceTypeList) {
		String ipAddress = discoverDeviceInfo.getIpAddress();
		boolean isDeviceIdentified = false;
		
		for (DeviceType deviceType : deviceTypeList) {
			if ((Boolean.TRUE == isDeviceIdentified)){
				break;
			}
			String user = deviceType.getDeviceDefaultCredential().getUsername();
			String password = deviceType.getDeviceDefaultCredential().getPassword();
			Credential credential = requestScopeDiscoveryCredential
					.getCredential(deviceType.getName());
			if (credential != null && !StringUtils.isEmpty(credential.getUserName())) {
				user = credential.getUserName();
				password = credential.getPassword();
			}
			Expect4j expect = null;
			try {
				expect = getExpect4j(ipAddress, user, password);
				expect.send(NEWLINE_COMMAND);
				expect.send(command);

				switch (DiscoveryDeviceIdentifierEnum.valueOf(deviceType.getIdentifyBy())) {
				case REGEX:
					isDeviceIdentified = doesResponseMatchesIdentifierPattern(expect, deviceType.getIdentifier());
					break;
				case REGULAR:
					isDeviceIdentified = doesResponseContainIdentifiers(expect, deviceType.getIdentifier());
					break;
				default:
					isDeviceIdentified = false;
					break;
				}
				expect.send(EXIT_COMMAND);
			} catch (Exception e) {
				logger.error("Identifier did not match for : " + ipAddress + "-->"
						+ deviceType.getName());
			} finally {
				if (expect != null)
					expect.close();
			}
		}
		
	}

	private static String getHttpResponse(String deviceUri) {
		String responseStr = null;
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
					.build();
			CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext)
					.setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
			RequestConfig config = RequestConfig.custom().setConnectTimeout(2 * 1000)
					.setConnectionRequestTimeout(1 * 1000).setSocketTimeout(1 * 1000).build();
			HttpGet httpget = new HttpGet(deviceUri);
			httpget.setConfig(config);
			try {
				responseStr = httpClient.execute(httpget, getResponseHandler());
			} finally {
				httpClient.close();
			}
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
			logger.error("Unable to connect : " + deviceUri);
		}
		return responseStr;
	}

	private static ResponseHandler<String> getResponseHandler() {
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse httpResponse) throws ClientProtocolException, IOException {
				int status = httpResponse.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = httpResponse.getEntity();
					if (entity != null)
						return EntityUtils.toString(entity);
				}
				return null;

			}
		};
		return responseHandler;
	}

	private static boolean doesResponseContainIdentifiers(String responseStr, List<Identifier> identifiersList) {
		if (StringUtils.isEmpty(responseStr)) {
			return false;
		}
		List<String> identifierStrList = new ArrayList<String>();
		for (Identifier identifier : identifiersList) {
			identifierStrList.add(identifier.getText());
		}
		String[] identifiers = identifierStrList.stream().toArray(String[]::new);
		return Arrays.stream(identifiers).parallel().allMatch(responseStr::contains);
	}

	private static boolean doesResponseContainIdentifiers(Expect4j expect, List<Identifier> identifiers)
			throws MalformedPatternException, Exception {
		boolean isDeviceIdentified = false;
		for (Identifier identifier : identifiers) {
			int returnVal = expect.expect(identifier.getText(), getClosure());
			if (returnVal < 0) {
				isDeviceIdentified = false;
				break;
			} else {
				isDeviceIdentified = true;
				continue;
			}
		}

		return isDeviceIdentified;
	}

	private static boolean doesResponseMatchesIdentifierPattern(String responseStr, List<Identifier> identifiersList) {
		if (StringUtils.isEmpty(responseStr)) {
			return false;
		}
		List<String> identifierStrList = new ArrayList<String>();
		for (Identifier identifier : identifiersList) {
			identifierStrList.add(identifier.getText());
		}
		String[] identifiers = identifierStrList.stream().toArray(String[]::new);
		return Arrays.stream(identifiers).parallel().allMatch(responseStr::matches);
	}

	private static boolean doesResponseMatchesIdentifierPattern(Expect4j expect, List<Identifier> identifiers)
			throws MalformedPatternException, Exception {
		boolean isDeviceIdentified = false;
		for (Identifier identifier : identifiers) {
			List<Match> pattern = new ArrayList<Match>();
			Match match = new RegExpMatch(identifier.getText(), getClosure());
			pattern.add(match);
			int returnVal = expect.expect(pattern);
			if (returnVal < 0) {
				isDeviceIdentified = false;
				break;
			} else {
				isDeviceIdentified = true;
				continue;
			}
		}
		return isDeviceIdentified;
	}

	private static Expect4j getExpect4j(String ipAddress, String user, String password) throws Exception {
		final int port = 22;
		JSch jsch = new JSch();
		final Session session = jsch.getSession(user, InetAddress.getByName(ipAddress).getHostAddress(), port);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setDaemonThread(true);
		session.connect(3 * 1000);
		ChannelShell channel = (ChannelShell) session.openChannel("shell");
		channel.setPtyType("vt102");

		Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream()) {
			@Override
			public void close() {
				super.close();
				session.disconnect();
			}
		};
		expect.setDefaultTimeout(2 * 1000);
		channel.connect(2 * 1000);
		return expect;
	}

	private static Closure getClosure() {
		Closure closure = new Closure() {
			StringBuffer closureBuffer = new StringBuffer();

			@Override
			public void run(ExpectState state) throws Exception {
				closureBuffer.append(state.getBuffer());
			}

			@Override
			public String toString() {
				return closureBuffer.toString();
			}
		};
		return closure;
	}

	public static RequestScopeDiscoveryCredential getRequestScopeDiscoveryCredential() {
		return requestScopeDiscoveryCredential;
	}

	@Autowired(required = true)
	public void setRequestScopeDiscoveryCredential(RequestScopeDiscoveryCredential requestScopeDiscoveryCredential) {
		DiscoverDeviceTypeUtil.requestScopeDiscoveryCredential = requestScopeDiscoveryCredential;
	}

}
