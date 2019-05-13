package com.nilangpatel.worldgdp;

import com.agapsys.agreste.AgresteApplication;
import com.agapsys.agreste.PersistenceService;
import com.nilangpatel.worldgdp.entities.User;
import javax.persistence.EntityManager;
import javax.servlet.annotation.WebListener;

@WebListener
public class Application extends AgresteApplication {

    public static Application getRunningInstance() {
        return (Application) AgresteApplication.getRunningInstance();
    }

    @Override
    public String getRootName() {
        return Defs.APP_NAME;
    }

    @Override
    public String getVersion() {
        return Defs.APP_VERSION;
    }


    @Override
    protected void onStart() {
        super.onStart();

        EntityManager em = getRegisteredService(PersistenceService.class).getEntityManager();
        em.getTransaction().begin();

        User.findOrCreate(em, "username", "password", Roles.SAMPLE_ROLE);
        User.findOrCreate(em, "username2", "password2");

        em.getTransaction().commit();
        em.close();
    }

}
