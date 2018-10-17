package no.nav.kontantstotte.api.rest;

import no.nav.kontantstotte.innsending.InnsendingService;
import no.nav.kontantstotte.innsending.Soknad;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Path("sendinn")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = {"acr=Level4"})
public class InnsendingResource {

    private final InnsendingService innsendingService;

    private final Logger logger = LoggerFactory.getLogger(InnsendingResource.class);

    @Inject
    public InnsendingResource(InnsendingService innsendingService) {
        this.innsendingService = innsendingService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response sendInnSoknad(@FormDataParam("soknad") Soknad soknad) {

        if (!soknad.erGyldig()) {
            logger.warn("Noen har forsøkt å sende inn en ugyldig søknad.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        soknad.markerInnsendingsTidspunkt();

        Response response = innsendingService.sendInnSoknad(soknad);

        return response.getStatus() != 200 ? response :
                Response
                        .ok(
                                soknad.innsendingsTidspunkt.toString(),
                                MediaType.APPLICATION_JSON_TYPE
                        )
                        .build();
    }
}
