package web.backend.lab4.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import web.backend.lab4.dto.ErrorDTO;
import web.backend.lab4.dto.PointDTO;
import web.backend.lab4.service.PointService;
import web.backend.lab4.auth.JwtProvider;

import java.util.List;

@Path("/point")
public class PointController {
    @Inject
    private PointService pointService;

    @Inject
    private JwtProvider jwtProvider;

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPoint(PointDTO pointDTO, @CookieParam("access_token") String accessToken) {
        String username = jwtProvider.getUsernameFromToken(accessToken);
        return pointService.addPoint(pointDTO, username)
                .map(result -> Response.status(Response.Status.OK).entity(result).build())
                .orElse(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorDTO.of("Error processing point"))
                        .build());
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPoints(@CookieParam("access_token") String accessToken) {
        Long userId = jwtProvider.getUserIdFromToken(accessToken);
        return pointService.getPoints(userId)
                .map(results -> Response.ok(results).build())
                .orElse(Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorDTO.of("User not found"))
                        .build());
    }
}