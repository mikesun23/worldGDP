package com.nilangpatel.worldgdp.services;

import com.agapsys.agreste.test.AgresteContainer;
import com.agapsys.agreste.test.MockedTransaction;
import com.agapsys.agreste.test.TestUtils;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.rcf.exceptions.NotFoundException;
import com.nilangpatel.worldgdp.entities.Property;
import com.nilangpatel.worldgdp.entities.Property.PropertyDto;
import com.nilangpatel.worldgdp.entities.User;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyServiceTest {

    private AgresteContainer ac;
    private EntityManager em;
    private MockedTransaction jpa;
    private PropertyService propertyService;

    @Before
    public void before() {
        ac = new AgresteContainer<>();
        ac.start();
        em = TestUtils.getApplicationEntityManager();
        jpa = new MockedTransaction(TestUtils.getApplicationEntityManager());
        propertyService = TestUtils.getApplicationService(PropertyService.class, true);
    }

    @After
    public void after() {
        if (jpa.getEntityManager().getTransaction().isActive())
            jpa.rollback();

        if (em.getTransaction().isActive())
            em.getTransaction().rollback();

        em.close();

        ac.stop();
    }

    @Test
    public void testList() {
        em.getTransaction().begin();
        User user = User.findOrCreate(em, "user", "password");
        em.getTransaction().commit();

        List<Property> properties = propertyService.list(jpa, user);
        Assert.assertEquals(0, properties.size());

        em.getTransaction().begin();
        new Property()
            .setOwner(user)
            .setKey("key")
            .save(em);
        em.getTransaction().commit();

        properties = propertyService.list(jpa, user);
        Assert.assertEquals(1, properties.size());
        Assert.assertEquals("key", properties.get(0).getKey());
    }

    @Test
    public void testGetById() {
        em.getTransaction().begin();
        User user = User.findOrCreate(em, "user", "password");
        em.getTransaction().commit();

        Property property = propertyService.get(jpa, user, 1l);
        Assert.assertNull(property);

        em.getTransaction().begin();
        property = new Property()
            .setOwner(user)
            .setKey("key")
            .save(em);
        em.getTransaction().commit();

        user = User.findOrCreate(jpa.getEntityManager(), "user", "password");
        property = propertyService.get(jpa, user, property.getId());
        Assert.assertNotNull(property);
        Assert.assertEquals("key", property.getKey());
    }

    @Test
    public void testGetByKey() {
        em.getTransaction().begin();
        User user = User.findOrCreate(em, "user", "password");
        em.getTransaction().commit();

        Property property = propertyService.get(jpa, user, "key");
        Assert.assertNull(property);

        em.getTransaction().begin();
        property = new Property()
            .setOwner(user)
            .setKey("key")
            .save(em);
        em.getTransaction().commit();

        user = User.findOrCreate(jpa.getEntityManager(), "user", "password");
        property = propertyService.get(jpa, user, property.getKey());
        Assert.assertNotNull(property);
        Assert.assertEquals("key", property.getKey());
    }

    @Test
    public void testCreate() {
        em.getTransaction().begin();
        User user = User.findOrCreate(em, "user", "password");
        em.getTransaction().commit();

        Assert.assertEquals(0, new FindBuilder<>(Property.class).find(em).size());
        Property property;
        PropertyDto dto;
        Throwable error;

        // Missing DTO...
        error = null;
        try {
            property = propertyService.create(jpa, user, null);
        } catch (InvalidDataException ex) {
            error = ex;
        }

        Assert.assertNotNull(error);
        Assert.assertEquals("Missing property DTO", error.getMessage());

        // Missing key attribute...
        error = null;
        try {
            dto = new PropertyDto();
            property = propertyService.create(jpa, user, dto);
        } catch (InvalidDataException ex) {
            error = ex;
        }

        Assert.assertNotNull(error);
        Assert.assertEquals("Missing attribute: \"key\"", error.getMessage());

        // Duplicate key...
        em.getTransaction().begin();
        property = new Property()
            .setOwner(user)
            .setKey("key")
            .save(em);
        em.getTransaction().commit();

        error = null;
        try {
            dto = new PropertyDto();
            dto.key = "key";
            property = propertyService.create(jpa, user, dto);
        } catch (InvalidDataException ex) {
            error = ex;
        }

        Assert.assertNotNull(error);
        Assert.assertEquals("Duplicate key: \"key\"", error.getMessage());

        // Valid...
        dto = new PropertyDto();
        dto.key = "key1";
        dto.value = "value";

        user = User.findOrCreate(jpa.getEntityManager(), "user", "password");
        property = propertyService.create(jpa, user, dto);

        Assert.assertNotNull(property);
        Assert.assertEquals("key1", property.getKey());
        Assert.assertEquals("value", property.getValue());
        Assert.assertSame(user, property.getOwner());

        jpa.commit();

        Assert.assertEquals(2, new FindBuilder<>(Property.class).find(em).size());
    }

    @Test
    public void testUpdate() {
        em.getTransaction().begin();
        User user = User.findOrCreate(em, "user", "password");
        em.getTransaction().commit();

        Throwable error;
        PropertyDto dto;

        // Missing property DTO...
        error = null;
        try {
            propertyService.update(jpa, user, null);
        } catch (InvalidDataException ex) {
            error = ex;
        }

        Assert.assertNotNull(error);
        Assert.assertEquals("Missing property DTO", error.getMessage());

        // ID not found...
        dto = new PropertyDto();
        dto.id = 18l;
        error = null;
        try {
            propertyService.update(jpa, user, dto);
        } catch (NotFoundException ex) {
            error = ex;
        }

        Assert.assertNotNull(error);
        Assert.assertEquals("ID not found: 18", error.getMessage());

        // Missing 'key' attribute...
        em.getTransaction().begin();
        Property prop1 = new Property().setKey("key").setOwner(user).save(em);
        em.getTransaction().commit();
        dto = new PropertyDto();
        dto.id = prop1.getId();
        error = null;
        try {
            propertyService.update(jpa, user, dto);
        } catch (InvalidDataException ex) {
            error = ex;
        }
        Assert.assertNotNull(error);
        Assert.assertEquals("Missing attribute: \"key\"", error.getMessage());

        // Valid...
        dto.key = prop1.getKey();
        dto.value = "new value";
        error = null;
        try {
            propertyService.update(jpa, user, dto);
        } catch (Throwable ex) {
            error = ex;
        }
        Assert.assertNull(error);

        // Duplicate key...
        em.getTransaction().begin();
        Property prop2 = new Property().setKey("key2").setOwner(user).save(em);
        em.getTransaction().commit();
        dto = new PropertyDto();
        dto.id = prop1.getId();
        dto.key = prop2.getKey(); // <-- duplicate key
        dto.value = "new value";
        error = null;
        try {
            propertyService.update(jpa, user, dto);
        } catch (InvalidDataException ex) {
            error = ex;
        }
        Assert.assertNotNull(error);
        Assert.assertEquals("Duplicate key: \"key2\"", error.getMessage());
    }

    @Test
    public void testDelete() {
        em.getTransaction().begin();
        User user1 = User.findOrCreate(em, "user", "password");
        User.findOrCreate(em, "user2", "password");
        em.getTransaction().commit();

        int count;

        count = propertyService.delete(jpa, user1, 1);
        Assert.assertEquals(0, count);;

        em.getTransaction().begin();
        Property prop = new Property().setKey("key").setOwner(user1).save(em);
        em.getTransaction().commit();

        User testuser1 = User.findOrCreate(jpa.getEntityManager(), "user", "password");
        User testuser2 = User.findOrCreate(jpa.getEntityManager(), "user2", "password");

        count = propertyService.delete(jpa, testuser2, prop.getId());
        Assert.assertEquals(0, count);

        count = propertyService.delete(jpa, testuser1, prop.getId());
        Assert.assertEquals(1, count);
    }
}
