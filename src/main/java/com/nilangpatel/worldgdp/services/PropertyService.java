package com.nilangpatel.worldgdp.services;

import com.agapsys.agreste.JpaTransaction;
import com.agapsys.jpa.DeleteBuilder;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.rcf.exceptions.NotFoundException;
import com.agapsys.web.toolkit.Service;
import com.nilangpatel.worldgdp.entities.Property;
import com.nilangpatel.worldgdp.entities.Property.PropertyDto;
import com.nilangpatel.worldgdp.entities.User;
import java.util.List;

public class PropertyService extends Service {

    public List<Property> list(JpaTransaction jpa, User user) {
        List<Property> list = new FindBuilder<>(Property.class).by("owner", user).find(jpa.getEntityManager());
        return list;
    }

    public Property get(JpaTransaction jpa, User user, Long id) {
        Property entity = new FindBuilder<>(Property.class).by("id", id).and("owner", user).findFirst(jpa.getEntityManager());
        return entity;
    }

    public Property get(JpaTransaction jpa, User user, String key) {
        return new FindBuilder<>(Property.class).by("key", key).and("owner", user).findFirst(jpa.getEntityManager());
    }

    public Property create(JpaTransaction jpa, User user, PropertyDto dto) {
        if (dto == null)
            throw new InvalidDataException("Missing property DTO");

        if (dto.key == null || dto.key.trim().isEmpty())
            throw new InvalidDataException("Missing attribute: \"%s\"", "key");

        try {
            if (get(jpa, user, dto.key) != null)
                throw new InvalidDataException("Duplicate key: \"%s\"", dto.key);

            Property property = new Property();
            property.setKey(dto.key);
            property.setValue(dto.value);
            property.setOwner(user);
            property.save(jpa.getEntityManager());
            return property;
        } catch (IllegalArgumentException ex) {
            throw new InvalidDataException(ex.getMessage());
        }
    }

    public Property update(JpaTransaction jpa, User user, PropertyDto dto) {
        try {
            if (dto == null)
                throw new InvalidDataException("Missing property DTO");

            Property entity = get(jpa, user, dto.id);

            if (entity == null) throw new NotFoundException("ID not found: %d", dto.id);
            if (dto.key == null || dto.key.trim().isEmpty()) throw new InvalidDataException("Missing attribute: \"key\"");
            if (!entity.getKey().equals(dto.key) && get(jpa, user, dto.key) != null) throw new InvalidDataException("Duplicate key: \"%s\"", dto.key);

            entity.setKey(dto.key);
            entity.setValue(dto.value);

            return entity;
        } catch (IllegalArgumentException ex) {
            throw new InvalidDataException(ex.getMessage());
        }
    }

    public int delete(JpaTransaction jpa, User user, long id) {
        return new DeleteBuilder<>(Property.class).where("id", id).and("owner", user).delete(jpa.getEntityManager());
    }

}
