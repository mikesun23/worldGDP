package com.nilangpatel.worldgdp.controllers;

import com.agapsys.agreste.AgresteController;
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.JsonRequest;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebActions;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.exceptions.NotFoundException;
import com.nilangpatel.worldgdp.Roles;
import com.nilangpatel.worldgdp.entities.Property;
import com.nilangpatel.worldgdp.entities.Property.PropertyDto;
import com.nilangpatel.worldgdp.entities.User;
import com.nilangpatel.worldgdp.services.PropertyService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

@WebController
public class PropertyController extends AgresteController {

    private PropertyService propertyService;

    @Override
    protected void onControllerInit() {
        super.onControllerInit();
        propertyService = getServiceOnDemand(PropertyService.class);
    }

    @WebAction(secured = true)
    public List<Property> list(JsonRequest request) throws ServletException, IOException {
        return propertyService.list(getJpaTransaction(request), (User) getUser(request));
    }

    @WebAction(secured = true)
    public Property get(ActionRequest request) throws ServletException, IOException {
        final String PARAM_ID = "id";
        Long id = request.getMandatoryParameter(Long.class, PARAM_ID);

        Property property = propertyService.get(getJpaTransaction(request), (User) getUser(request), id);
        if (property == null)
            throw new NotFoundException("ID not found: %d", id);

        return property;
    }

    @WebAction(secured = true, httpMethods = HttpMethod.POST)
    public Property create(JsonRequest request) throws ServletException, IOException {
        PropertyDto propertyDto = request.readObject(PropertyDto.class);
        return propertyService.create(getJpaTransaction(request), (User) getUser(request), propertyDto);
    }

    @WebAction(secured = true, httpMethods = HttpMethod.PUT)
    public Property update(JsonRequest  request) throws IOException, ServletException {
        PropertyDto propertyDto = request.readObject(PropertyDto.class);
        return propertyService.update(getJpaTransaction(request), (User) getUser(request), propertyDto);
    }

    @WebAction(requiredRoles = {Roles.SAMPLE_ROLE}, httpMethods = HttpMethod.DELETE) // <-- secured due to role
    public void delete(ActionRequest request) throws ServletException, IOException {
        final String PARAM_ID = "id";
        Long id = request.getMandatoryParameter(Long.class, PARAM_ID);

        if (propertyService.delete(getJpaTransaction(request), (User) getUser(request), id) == 0)
            throw new NotFoundException("ID not found: %d", id);
    }

    @WebActions({
        @WebAction(mapping = "/"),
        @WebAction(mapping = "/index")
    })
    public void index(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.println("PROPERTY controller\n\nAvailable endpoints (requires a logged user):");
        writer.println("\tGET  property/index");
        writer.println("\tGET  property/list");
        writer.println("\tGET  property/get");
        writer.println("\tPOST property/create");
        writer.println("\tPUT  property/update");
    }
}
