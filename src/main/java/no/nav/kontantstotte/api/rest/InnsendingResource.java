package no.nav.kontantstotte.api.rest;

import no.nav.kontantstotte.innsending.Soknad;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import static java.time.LocalDateTime.now;

@Component
@Path("sendinn")
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
public class InnsendingResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void sendInnSoknad(@FormDataParam("soknad") Soknad soknad) {
        soknad.innsendingTimestamp = now();

    }
}
