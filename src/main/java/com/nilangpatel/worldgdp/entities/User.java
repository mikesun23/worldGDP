package com.nilangpatel.worldgdp.entities;

import com.agapsys.jpa.AbstractEntity;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.rcf.Controller.Dto;
import com.nilangpatel.worldgdp.entities.User.UserDto;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.mindrot.jbcrypt.BCrypt;

@Entity(name = "usr")
public class User extends AbstractEntity<User> implements com.agapsys.rcf.User, Dto {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static class UserDto {
        public Long id;
        public String username;
        public Set<String> roles;

        public UserDto() {}
        public UserDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.roles =  user.getRoles();
        }
    }

    /**
     * Finds a user with given username. If it does not exist, create a new one with given parameters.
     * @param em Entity manager
     * @param username username
     * @param password user password
     * @param roles roles associated roles
     * @return user
     */
    public static User findOrCreate(EntityManager em, String username, String password, String...roles) {
        User user = new FindBuilder<>(User.class).by("username", username).findFirst(em);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(password);
            for (String role : roles) {
                if (role != null && !role.trim().isEmpty()) {
                    user.addRole(role);
                }
            }
            user.save(em);
        }

        return user;
    }
    // =========================================================================
    // </editor-fold>

    // ID ----------------------------------------------------------------------
    @Id
    @GeneratedValue
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    public User setId(long id) {
        this.id = id;
        return this;
    }
    // -------------------------------------------------------------------------

    // Username ----------------------------------------------------------------
    @Column(unique = true)
    private String username;

    public String getUsername() {
        return username;
    }
    public User setUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty username");

        this.username = username;
        return this;
    }
    // -------------------------------------------------------------------------

    // Password ----------------------------------------------------------------
    protected String getPasswordHash(String password) {
        int logRounds = 4;
        return (BCrypt.hashpw(password, BCrypt.gensalt(logRounds)));
    }

    protected boolean isPasswordValid(String password, String passwordHash) {
        return BCrypt.checkpw(password, passwordHash);
    }

    private String passwordHash;
    public String getPasswordHash() {
        return passwordHash;
    }
    public User setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) throw new IllegalArgumentException("Null/Empty password hash");

        this.passwordHash = passwordHash;
        return this;
    }
    public final User setPassword(String password) {
        return setPasswordHash(getPasswordHash(password));
    }
    public final boolean isPasswordValid(String password) {
        return isPasswordValid(password, getPasswordHash());
    }
    // -------------------------------------------------------------------------

    // Roles -------------------------------------------------------------------
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles = new LinkedHashSet<>();

    @Override
    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        if (roles == null)
            throw new IllegalArgumentException("Role set cannot be null");

        this.roles = roles;
    }
    public final void setRoles(String...roles) {
        setRoles(new LinkedHashSet<>(Arrays.asList(roles)));
    }

    public void addRole(String...roles) {
        int i = 0;
        for (String role : roles) {
            if (role == null) throw new IllegalArgumentException("Null role at index " + i);

            getRoles().add(role);
            i++;
        }
    }
    public void clearRoles() {
        getRoles().clear();
    }
    // -------------------------------------------------------------------------

    // Permissions
    private long permissions;

    @Override
    public long getPermissions() {
        return permissions;
    }

    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }
    // -------------------------------------------------------------------------

    @Override
    public Object getDto() {
        return new UserDto(this);
    }

}
