package no.nav.kontantstotte.api.rest;

import com.nimbusds.jwt.SignedJWT;
import no.nav.kontantstotte.config.ApplicationConfig;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { ApplicationConfig.class, TokenGeneratorConfiguration.class })
public class InnsendingResourceTest {

    @Value("${local.server.port}")
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;


    // TODO: I denne testen deserialiseres søknaden til null - dette skjer ikke i browser, dvs at noe er galt med hvordan FormDataMultiPart-objektet er bygd opp.
    // Må fikses, hvis ikke får man NullpointerException.
    @Test
    @Ignore
    public void testInnsendingAvSoknad() {
        WebTarget target = ClientBuilder.newClient()
                .target("http://localhost:" + port + contextPath)
                .register(MultiPartFeature.class);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT("12345678911");

        FormDataMultiPart mp = new FormDataMultiPart();
        FormDataBodyPart soknadForm = new FormDataBodyPart("soknad", testSoknadJson());
        mp.bodyPart(soknadForm, MediaType.APPLICATION_JSON_TYPE);

        Response response = target.path("/sendinn")
                .request()
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildPost(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE))
                .invoke();

        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
    }

    private String testSoknadJson() {
        return "{\"arbeidsforhold\": {}, \"barn\": {}, \"barnehageplass\": {}, \"familieforhold\": {}, \"sokerKrav\": {}}";
    }

}
