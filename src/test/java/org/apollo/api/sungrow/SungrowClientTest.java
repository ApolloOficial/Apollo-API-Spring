package org.apollo.api.sungrow;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SungrowClientTest {
    private MockRestServiceServer server;
    private SungrowClient client;

    @BeforeEach
    void setUp() {
        SungrowProperties properties = new SungrowProperties();
        properties.setAppkey("app-key");
        properties.setAccessKey("access-key");
        properties.setRedirectUri("https://apollo.test/callback");
        RestClient.Builder builder = RestClient.builder().baseUrl("http://sungrow.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SungrowClient(builder.build(), properties);
    }

    @Test
    void shouldExchangeTheAuthorizationCodeForTokens() {
        server.expect(requestTo("http://sungrow.test/openapi/apiManage/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-access-key", "access-key"))
                .andExpect(headerDoesNotExist("Authorization"))
                .andExpect(content().string(containsString("\"code\":\"abc\"")))
                .andExpect(content().string(containsString("\"grant_type\":\"authorization_code\"")))
                .andExpect(content().string(containsString("\"appkey\":\"app-key\"")))
                .andRespond(withSuccess("""
                        {"req_serial_num": "1", "result_code": "1", "result_msg": "success",
                         "result_data": {"access_token": "AT", "refresh_token": "RT", "expires_in": 172799,
                                         "token_type": "bearer", "auth_ps_list": [111, 222], "auth_user": "u"}}
                        """, MediaType.APPLICATION_JSON));

        SungrowTokenResponse.TokenData data = client.exchangeCode("abc");

        assertEquals("AT", data.accessToken());
        assertEquals("RT", data.refreshToken());
        assertEquals(172799L, data.expiresIn());
        assertEquals(2, data.authPsList().size());
        server.verify();
    }

    @Test
    void shouldRefreshWithTheRefreshToken() {
        server.expect(requestTo("http://sungrow.test/openapi/apiManage/refreshToken"))
                .andExpect(content().string(containsString("\"refresh_token\":\"RT\"")))
                .andRespond(withSuccess("""
                        {"result_code": "1", "result_msg": "success",
                         "result_data": {"access_token": "AT2", "refresh_token": "RT2", "expires_in": 100, "code": 1}}
                        """, MediaType.APPLICATION_JSON));

        assertEquals("RT2", client.refresh("RT").refreshToken());
    }

    @Test
    void shouldFailWhenTheRefreshTokenIsRejected() {
        server.expect(requestTo("http://sungrow.test/openapi/apiManage/refreshToken"))
                .andRespond(withSuccess("""
                        {"result_code": "1", "result_data": {"access_token": "A", "refresh_token": "R", "code": -2}}
                        """, MediaType.APPLICATION_JSON));

        SungrowApiException exception = assertThrows(SungrowApiException.class, () -> client.refresh("RT"));

        assertEquals("REFRESH_-2", exception.getCode());
    }

    @Test
    void shouldSendTheBearerTokenAndReadTheRealTimePoints() {
        server.expect(requestTo("http://sungrow.test/openapi/platform/getDeviceRealTimeData"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer AT"))
                .andExpect(header("x-access-key", "access-key"))
                .andExpect(content().string(containsString("\"sn_list\":[\"SN1\"]")))
                .andExpect(content().string(containsString("\"point_id_list\":[\"96\",\"70\"]")))
                .andExpect(content().string(containsString("\"device_type\":1")))
                .andRespond(withSuccess("""
                        {"result_code": "1", "result_msg": "success",
                         "result_data": {"device_point_list": [
                            {"device_sn": "SN1", "device_time": "20261023120000", "dev_fault_status": "4",
                             "p96": "600.1", "p70": "8.0"}]}}
                        """, MediaType.APPLICATION_JSON));

        List<Map<String, Object>> devices = client.realTime("AT", List.of("SN1"), List.of("96", "70"));

        assertEquals(1, devices.size());
        assertEquals("SN1", devices.get(0).get("device_sn"));
        assertEquals("600.1", devices.get(0).get("p96"));
        server.verify();
    }

    @Test
    void shouldReturnAnEmptyListWhenThereIsNoData() {
        server.expect(requestTo("http://sungrow.test/openapi/platform/getDeviceRealTimeData"))
                .andRespond(withSuccess("{\"result_code\": \"1\", \"result_data\": {}}", MediaType.APPLICATION_JSON));

        assertTrue(client.realTime("AT", List.of("SN1"), List.of("96")).isEmpty());
    }

    @Test
    void shouldReportAnInvalidTokenFromTheErrorBody() {
        server.expect(requestTo("http://sungrow.test/openapi/platform/getDeviceRealTimeData"))
                .andRespond(withSuccess("{\"error\": \"invalid_token\", \"error_description\": \"expired\"}",
                        MediaType.APPLICATION_JSON));

        SungrowApiException exception = assertThrows(SungrowApiException.class,
                () -> client.realTime("AT", List.of("SN1"), List.of("96")));

        assertTrue(exception.isInvalidToken());
        assertFalse(exception.isRateLimited());
    }

    @Test
    void shouldReportApiErrorCodes() {
        server.expect(requestTo("http://sungrow.test/openapi/platform/getDeviceRealTimeData"))
                .andRespond(withSuccess("{\"result_code\": \"E999\", \"result_msg\": \"hourly limit\"}",
                        MediaType.APPLICATION_JSON));

        SungrowApiException exception = assertThrows(SungrowApiException.class,
                () -> client.realTime("AT", List.of("SN1"), List.of("96")));

        assertTrue(exception.isRateLimited());
        assertEquals("E999", exception.getCode());
    }

    @Test
    void shouldFailWhenTheServerAnswersWithoutAReadableBody() {
        server.expect(requestTo("http://sungrow.test/openapi/platform/getDeviceRealTimeData"))
                .andRespond(withServerError());

        SungrowApiException exception = assertThrows(SungrowApiException.class,
                () -> client.realTime("AT", List.of("SN1"), List.of("96")));

        assertEquals("HTTP_500", exception.getCode());
    }
}
