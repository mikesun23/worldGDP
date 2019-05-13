package com.nilangpatel.worldgdp.controllers;

import com.agapsys.agreste.AgresteController;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.ActionResponse;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.JsonRequest;
import com.agapsys.rcf.JsonResponse;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebActions;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.exceptions.BadRequestException;
import com.agapsys.rcf.exceptions.ForbiddenException;
import com.nilangpatel.worldgdp.entities.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

@WebController
public class UserController extends AgresteController {
    
    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static class LoginFormDto {
        public String username;
        public String password;
    }
    // =========================================================================
    // </editor-fold>
    
    @WebActions({
        @WebAction(mapping="/"),
        @WebAction
    })
    public void index(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.printf("USER controller (default: username: %s, password: %s)\n\nAvailable endpoints (requires a logged user):\n", "username", "password");
        writer.println("\tGET  user/index");
        writer.println("\tGET  user/login"); // <-- in a real application, POST is more appropriate.
        writer.println("\tGET  user/logout"); // <-- in a real application, POST is more appropriate.
        writer.println("\tGET  user/me");
    }

    @WebAction(httpMethods = HttpMethod.POST)
    public User login(JsonRequest request, JsonResponse response) throws IOException, ServletException {

        LoginFormDto loginFormDto = request.readObject(LoginFormDto.class);

        if (loginFormDto == null)
            throw new BadRequestException("Missing login data");

        if (loginFormDto.username == null || loginFormDto.username.trim().isEmpty())
            throw new BadRequestException("Missing parameter: %s", "username");

        if (loginFormDto.password == null || loginFormDto.password.trim().isEmpty())
            throw new BadRequestException("Missing parameter: %s", "password");

        User user = new FindBuilder<>(User.class).by("username", loginFormDto.username).findFirst(getJpaTransaction(request).getEntityManager());

        if (user == null || !user.isPasswordValid(loginFormDto.password)) {
            setUser(request, response, null);
            throw new ForbiddenException("Invalid credentials");
        }

        setUser(request, response, user);
        return user;
    }

    @WebAction
    public void logout(ActionRequest request, ActionResponse response) throws ServletException, IOException {
        setUser(request, response, null);
    }

    @WebAction(secured = true)
    public User me(ActionRequest request) throws ServletException, IOException {
        return (User) getUser(request);
    }

}
