
package com.nilangpatel.worldgdp.controllers;

import com.agapsys.agreste.test.AgresteContainer;
import com.agapsys.agreste.test.TestUtils;
import com.agapsys.agreste.test.TestUtils.Endpoint;
import com.agapsys.agreste.test.TestUtils.JsonEndpoint;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.rcf.HttpMethod;
import com.nilangpatel.worldgdp.Application;
import com.nilangpatel.worldgdp.controllers.UserControllerTest.LoginInfo;
import com.nilangpatel.worldgdp.entities.Property;
import com.nilangpatel.worldgdp.entities.Property.PropertyDto;
import com.nilangpatel.worldgdp.entities.User;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyControllerTest {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static Property createProperty(EntityManager em, long userId, String key, String value) {
        User user = em.find(User.class, userId);
        Property property = new Property();
        property.setOwner(user);
        property.setKey(key);
        property.setValue(value);
        property.save(em);
        return property;
    }
    // =========================================================================
    // </editor-fold>

    private AgresteContainer ac;
    private EntityManager em;

    @Before
    public void before() {
        ac = new AgresteContainer<>(Application.class);
        ac.start();
        em = TestUtils.getApplicationEntityManager();
    }

    @After
    public void after() {
        ac.stop();
    }

    @Test
    public void indexTest() {
        Endpoint endpoint;
        StringResponse resp;

        endpoint = new Endpoint(HttpMethod.GET, "/property/");
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(200, resp);

        endpoint = new Endpoint(HttpMethod.GET, "/property/index");
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(200, resp);
    }

    @Test
    public void testList() {
        Endpoint endpoint = new Endpoint(HttpMethod.GET, "/property/list");
        StringResponse resp;

        List<PropertyDto> properties;
        LoginInfo loginInfo;

        // Unlogged access...
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged access...
        loginInfo = UserControllerTest.doLogin(ac, "username", "password");

        // Retrieving an empty list (initially there is no properties)...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        properties = TestUtils.readJsonList(PropertyDto.class, resp);
        Assert.assertEquals(0, properties.size());

        // Creating some properties...
        em.getTransaction().begin();
        createProperty(em, loginInfo.getUserDto().id, "key1", "value1");
        createProperty(em, loginInfo.getUserDto().id, "key2", "value2");
        em.getTransaction().commit();

        // Getting list again...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        properties = TestUtils.readJsonList(PropertyDto.class, resp);
        Assert.assertEquals(2, properties.size());
        Assert.assertEquals("key1", properties.get(0).key);
        Assert.assertEquals("value1", properties.get(0).value);
        Assert.assertEquals("key2", properties.get(1).key);
        Assert.assertEquals("value2", properties.get(1).value);
    }

    @Test
    public void testGet() {
        Endpoint endpoint = new Endpoint(HttpMethod.GET, "/property/get");
        StringResponse resp;

        PropertyDto propertyDto;
        LoginInfo loginInfo;

        // Unlogged access...
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged access...
        loginInfo = UserControllerTest.doLogin(ac, "username", "password");

        // Missing id...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        TestUtils.assertErrorStatus(400, "Missing parameter: id", resp);
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%s", ""));
        TestUtils.assertErrorStatus(400, "Cannot convert \"\" into java.lang.Long", resp);

        // Id not found...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%s", 18)); // <-- initially, there is no properties
        TestUtils.assertStatus(404, resp);

        // Creating some entries...
        em.getTransaction().begin();
        Property prop1 = createProperty(em, loginInfo.getUserDto().id, "key1", "value1");
        Property prop2 = createProperty(em, loginInfo.getUserDto().id, "key2", "value2");
        em.getTransaction().commit();

        // Getting again (prop1)...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%s", prop1.getId()));
        propertyDto = TestUtils.readJsonObject(PropertyDto.class, resp);
        Assert.assertEquals(prop1.getKey(), propertyDto.key);
        Assert.assertEquals(prop1.getValue(), propertyDto.value);

        // Getting again (prop2)...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%s", prop2.getId()));
        propertyDto = TestUtils.readJsonObject(PropertyDto.class, resp);
        Assert.assertEquals(prop2.getKey(), propertyDto.key);
        Assert.assertEquals(prop2.getValue(), propertyDto.value);
    }

    @Test
    public void testCreate() {
        JsonEndpoint endpoint = new JsonEndpoint(HttpMethod.POST, "/property/create");
        StringResponse resp;
        PropertyDto propertyDto;
        PropertyDto responseDto;

        LoginInfo loginInfo;

        // Unlogged access...
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged access...
        loginInfo = UserControllerTest.doLogin(ac, "username", "password");

        // Missing propertyDto...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        TestUtils.assertErrorStatus(400, "Missing property DTO", resp);

        // Missing property key...
        propertyDto = new PropertyDto();
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertErrorStatus(400, "Missing attribute: \"key\"", resp);

        // Valid property...
        propertyDto = new PropertyDto();
        propertyDto.key = "key";
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertStatus(200, resp);
        responseDto = TestUtils.readJsonObject(PropertyDto.class, resp);
        Assert.assertEquals("key", responseDto.key);

        // Duplicate key...
        propertyDto = new PropertyDto();
        propertyDto.key = "key";
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertErrorStatus(400, "Duplicate key: \"key\"", resp);
    }

    @Test
    public void testUpdate() {
        JsonEndpoint endpoint = new JsonEndpoint(HttpMethod.PUT, "/property/update");
        StringResponse resp;
        PropertyDto propertyDto;
        PropertyDto responseDto;

        LoginInfo loginInfo;

        // Unlogged access...
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged access...
        loginInfo = UserControllerTest.doLogin(ac, "username", "password");

        // Missing propertyDto...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        TestUtils.assertErrorStatus(400, "Missing property DTO", resp);

        // ID not found...
        propertyDto = new PropertyDto();
        propertyDto.id = 18l;
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertErrorStatus(404, "ID not found: 18", resp);

        // Creating some entries...
        em.getTransaction().begin();
        Property prop1 = createProperty(em, loginInfo.getUserDto().id, "key1", "value1");
        createProperty(em, loginInfo.getUserDto().id, "key2", "value2");
        em.getTransaction().commit();

        // Missing key...
        propertyDto = new PropertyDto();
        propertyDto.id = prop1.getId();
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertErrorStatus(400, "Missing attribute: \"key\"", resp);

        // Valid key...
        propertyDto = new PropertyDto();
        propertyDto.id = prop1.getId();
        propertyDto.key = "new-key";
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertStatus(200, resp);
        responseDto = TestUtils.readJsonObject(PropertyDto.class, resp);
        Assert.assertEquals("new-key", responseDto.key);

        // Duplicate key...
        propertyDto = new PropertyDto();
        propertyDto.id = prop1.getId();
        propertyDto.key = "key2"; // <-- duplicate
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest(propertyDto));
        TestUtils.assertErrorStatus(400, "Duplicate key: \"key2\"", resp);

    }

    @Test
    public void testDelete() {
        Endpoint endpoint = new Endpoint(HttpMethod.DELETE, "/property/delete");
        StringResponse resp;

        LoginInfo loginInfo;

        // Unlogged access...
        resp = ac.doRequest(endpoint.getRequest());
        TestUtils.assertStatus(401, resp);

        // Logged with insufficient privileges
        loginInfo = UserControllerTest.doLogin(ac, "username2", "password2");
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest());
        TestUtils.assertStatus(403, resp);

         // Logged with privileges...
        loginInfo = UserControllerTest.doLogin(ac, "username", "password");

        // ID not found...
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%d", 18));
        TestUtils.assertStatus(404, resp);

        // Valid id...
        em.getTransaction().begin();
        Property prop = createProperty(em, loginInfo.getUserDto().id, "key1", "value1");
        em.getTransaction().commit();
        List<Property> properties = new FindBuilder<>(Property.class).by("owner.id", loginInfo.getUserDto().id).find(em);
        Assert.assertEquals(1, properties.size());
        resp = ac.doRequest(loginInfo.getHttpClient(), endpoint.getRequest("id=%d", prop.getId()));
        TestUtils.assertStatus(200, resp);

        properties = new FindBuilder<>(Property.class).by("owner.id", loginInfo.getUserDto().id).find(em);
        Assert.assertEquals(0, properties.size());

    }

}
