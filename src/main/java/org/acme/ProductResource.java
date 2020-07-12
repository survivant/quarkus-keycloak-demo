package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/products")
@RolesAllowed("user")
public class ProductResource {

    @Inject
    Template products;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance products() {

        return products.data("username", "mysuername");
    }
}