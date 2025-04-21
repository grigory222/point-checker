package web.backend.lab4.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import web.backend.lab4.dto.ErrorDTO;
import web.backend.lab4.dto.UserDTO;
import web.backend.lab4.service.AuthService;

@Path("/auth")
public class AuthController {
    @Inject
    private AuthService authService;

    @POST
    @Path("/signup")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    public Response signup(@Valid UserDTO userDTO) {
        return authService.signup(userDTO)
                .map(error -> Response.status(Response.Status.BAD_REQUEST).entity(error).build())
                .orElse(Response.ok().build());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(@Valid UserDTO userDTO) {
        return authService.login(userDTO)
                .map(response -> Response.ok()
                        .cookie(response.accessTokenCookie(), response.refreshTokenCookie())
                        .build())
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorDTO.of("Wrong username or password"))
                        .build());
    }

    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(@CookieParam("refresh_token") String refreshToken) {
        return authService.refreshToken(refreshToken)
                .map(response -> Response.ok()
                        .cookie(response.accessTokenCookie())
                        .build())
                .orElse(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(ErrorDTO.of("Invalid or expired refresh token"))
                        .build());
    }

    @POST
    @Path("/logout")
    public Response logout() {
        AuthService.AuthResponse response = authService.logout();
        return Response.ok()
                .cookie(response.accessTokenCookie(), response.refreshTokenCookie())
                .entity("{\"message\": \"Logged out successfully\"}")
                .build();
    }
}