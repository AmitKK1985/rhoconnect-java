package com.msi.rhoconnect.api;

import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.msi.rhoconnect.api.SystemResource;
import com.sun.jersey.api.client.ClientResponse;

import org.junit.Rule;
import org.junit.ClassRule;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertThat;

public class SystemResourceTest {
	@ClassRule
	@Rule
	public static WireMockRule wireMockRule = new WireMockRule(8089);
	static String URL = "http://localhost:8089";
	static String token;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		stubFor(post(urlEqualTo("/rc/v1/system/login"))
	             .withHeader("Content-Type", equalTo("application/json"))
					.willReturn(aResponse()
		                .withStatus(200)
		                .withBody("my-rhoconnect-token")));
		stubFor(post(urlEqualTo("/rc/v1/system/reset"))
				.withHeader("Content-Type", equalTo("application/json"))
		        .withHeader("X-RhoConnect-API-TOKEN", equalTo(token))
					.willReturn(aResponse()
		                .withStatus(200)
		                .withBody("DB reset")));
		stubFor(get(urlEqualTo("/rc/v1/system/license"))
		        .withHeader("X-RhoConnect-API-TOKEN", equalTo(token))
				.willReturn(aResponse()
	                .withStatus(200)
	                .withHeader("Content-Type", "application/json")
	                .withBody("{\"rhoconnect_version\":\"Version 1\",\"licensee\":\"Rhomobile\",\"seats\":10,\"issued\":\"Fri Apr 23 17:20:13 -0700 2010\",\"available\":10}")));
			
		stubFor(post(urlEqualTo("/rc/v1/system/appserver"))
				.withHeader("Content-Type", equalTo("application/json"))
			    .withHeader("X-RhoConnect-API-TOKEN", equalTo(token))
					.willReturn(aResponse()
		                .withStatus(200)
		                .withHeader("Content-Type", "application/json")
		                .withBody("{\"adapter_url\":\"http://localhost:3000\"}")));
		stubFor(get(urlEqualTo("/rc/v1/system/appserver"))
			    .withHeader("X-RhoConnect-API-TOKEN", equalTo(token))
					.willReturn(aResponse()
		                .withStatus(200)
		                .withHeader("Content-Type", "application/json")
		                .withBody("{\"adapter_url\":\"http://localhost:3000\"}")));
			
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogin() {
		ClientResponse response = SystemResource.login(URL, "");
		assertEquals("Response code", 200, response.getStatus());
		String token = response.getEntity(String.class);
		SystemResourceTest.token = token;
	}

	@Test
	public void testReset() {
		ClientResponse response = SystemResource.reset(URL, token);
		assertEquals("Response code", 200, response.getStatus());
		String body = response.getEntity(String.class);
		//System.out.println(body);
		assertEquals("Body", "DB reset", body);
	}
	
	@Test
	public void testLicense() {
		
		ClientResponse response = SystemResource.license(URL, token);
		assertEquals("Response code", 200, response.getStatus());
		String body = response.getEntity(String.class);
		JSONObject o = (JSONObject)JSONValue.parse(body);
		assertEquals("rhoconnect_version", "Version 1", o.get("rhoconnect_version"));
		assertEquals("licensee", "Rhomobile", o.get("licensee"));
		assertEquals("seats", new Long(10), o.get("seats"));
		assertEquals("issued", "Fri Apr 23 17:20:13 -0700 2010", o.get("issued"));
	}
	
	@Test
	public void testSetAppServer() {
		ClientResponse response = SystemResource.setAppserver(URL, token, "http://localhost:3000");
		assertEquals("Response code", 200, response.getStatus());

		response = SystemResource.getAppserver(URL, token);
		String body = response.getEntity(String.class);
		Object obj=JSONValue.parse(body);
		JSONObject o = (JSONObject)obj;
		assertEquals("adapter_url", "http://localhost:3000", o.get("adapter_url"));
	}

	@Test
	@Ignore
	public void testStats() {
		// TODO: enable stats and add sources 
		ClientResponse response = SystemResource.statsMetricPattern(URL, token, "source:*:Product");
		assertEquals("Response code", 200, response.getStatus());
		
		// TODO:
		// ?names=pattern
		// ?metric=foo&start=0&stop=-1
		//
		// RestClient.get(url,{'X-RhoConnect-API-TOKEN' => token, :params => {:names=>'source:*:Product'}})
		// => "[\"source:query:Product\"]"
		//
	}
	
}
