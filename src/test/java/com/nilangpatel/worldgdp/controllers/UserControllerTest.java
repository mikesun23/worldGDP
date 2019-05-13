package com.nilangpatel.worldgdp.controllers;

import com.agapsys.agreste.test.AgresteContainer;
import com.agapsys.agreste.test.TestUtils;
import com.agapsys.agreste.test.TestUtils.Endpoint;
import com.agapsys.agreste.test.TestUtils.JsonEndpoint;
import com.agapsys.http.HttpClient;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.http.utils.Pair;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.HttpMethod;
import com.nilangpatel.worldgdp.Application;
import com.nilangpatel.worldgdp.controllers.UserController.LoginFormDto;
import com.nilangpatel.worldgdp.entities.User.UserDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserControllerTest {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static class LoginInfo extends Pair<HttpClient, UserDto> {

        public LoginInfo(HttpClient first, UserDto second) {
            super(first, second);
        }

        public HttpClient getHttpClient() {
            return super.getFirst();
        }

        public UserDto getUserDto() {
            return super.getSecond();
        }
    }

    public static LoginInfo doLogin(AgresteContainer sc, String username, String password) {
        JsonEndpoint endpoint = new JsonEndpoint(HttpMethod.POST, "/user/login");

        LoginFormDto dto = new LoginFormDto();
        dto.username = username;
        dto.password = password;

        HttpClient client = new HttpClient();

        StringResponse resp = sc.doRequest(client, endpoint.getRequest(dto));
        TestUtils.assertStatus(200, resp);

        client.addDefaultHeader(Controller.XSRF_HEADER, resp.getCookie(Controller.XSRF_COOKIE).value);

        return new LoginInfo(client, TestUtils.readJsonObject(UserDto.class, resp));
    }
    // =========================================================================
    // </editor-fold>

    private AgresteContainer ac;

    @Before
    public void before() {
        ac = new AgresteContainer<>(Application.class);
        ac.start();
    }

    @After
    public void after() {
        ac.stop();
    }

    @Test
    public void indexTest() {
        Endpoint endpoint;
        StringResponse resp;

        endpoint = new Endpoint(HttpMethod.GET, "/user/");
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(200, resp);

        endpoint = new Endpoint(HttpMethod.GET, "/user/index");
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(200, resp);
    }

    @Test
    public void loginTest() {
        JsonEndpoint endpoint = new JsonEndpoint(HttpMethod.POST, "/user/login");
        StringResponse resp;
        LoginFormDto loginForm = new LoginFormDto();

        // Missing login data
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertErrorStatus(400, "Missing login data", resp);

        // Missing username
        resp = ac.doRequest(endpoint.getRequest(loginForm));
        TestUtils.assertErrorStatus(400, "Missing parameter: username", resp);

        // Missing password
        loginForm.username = "username";
        resp = ac.doRequest(endpoint.getRequest(loginForm));
        TestUtils.assertErrorStatus(400, "Missing parameter: password", resp);

        // Invalid credentials
        loginForm.username = "invalidUsername";
        loginForm.password = "invalidPassword";

        resp = ac.doRequest(endpoint.getRequest(loginForm));
        TestUtils.assertErrorStatus(403, "Invalid credentials", resp);

        // Valid credentials
        loginForm.username = "username";
        loginForm.password = "password";
        resp = ac.doRequest(endpoint.getRequest(loginForm));
        TestUtils.assertStatus(200, resp);
    }

    @Test
    public void testLogout() {
        Endpoint meEndpoint = new Endpoint(HttpMethod.GET, "/user/me");
        Endpoint logoutEndpoint = new Endpoint(HttpMethod.GET, "/user/logout");

        StringResponse resp;

        // Unlogged access
        resp = ac.doRequest(meEndpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged access
        LoginInfo loginInfo = doLogin(ac, "username", "password");
        Assert.assertEquals("username", loginInfo.getUserDto().username);

        resp = ac.doRequest(loginInfo.getHttpClient(), meEndpoint.getRequest());
        TestUtils.assertStatus(200, resp);
        Assert.assertEquals("username", TestUtils.readJsonObject(UserDto.class, resp).username);

        // Logout
        resp = ac.doRequest(loginInfo.getHttpClient(), logoutEndpoint.getRequest());
        TestUtils.assertStatus(200, resp);
        resp = ac.doRequest(loginInfo.getHttpClient(), meEndpoint.getRequest());
        TestUtils.assertStatus(401, resp);
    }

}
