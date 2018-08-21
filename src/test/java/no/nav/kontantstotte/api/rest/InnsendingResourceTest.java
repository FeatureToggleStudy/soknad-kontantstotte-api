package no.nav.kontantstotte.api.rest;

import com.nimbusds.jwt.SignedJWT;
import no.nav.kontantstotte.config.ApplicationConfig;
import no.nav.kontantstotte.pdf.PdfService;
import no.nav.security.oidc.OIDCConstants;
import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    @Test
    public void testInnsendingAvSoknad() {
        WebTarget target = ClientBuilder.newClient().target("http://localhost:" + port + contextPath);
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT("12345678911");
        Response response = target.path("/sendinn")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildPost(Entity.json(testSoknadJson()))
                .invoke();

        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
    }

    @Test
    public void testPdfGenerering() {
        PdfService pdfService = new PdfService();
        String html = pdfService.genererHtmlForPdf();

        WebTarget target = ClientBuilder.newClient().target("http://localhost:8080/");
        SignedJWT signedJWT = JwtTokenGenerator.createSignedJWT("12345678911");
        Response response = target.path("api/convert")
                .request()
                .header(OIDCConstants.AUTHORIZATION_HEADER, "Bearer " + signedJWT.serialize())
                .buildPost(Entity.entity(html, MediaType.TEXT_HTML)).invoke();

        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));

        try {
            new File("/Users/martineenger/nav/soknad-kontantstotte-api/TEST.pdf");
            OutputStream out = new FileOutputStream("/Users/martineenger/nav/soknad-kontantstotte-api/TEST.pdf");
            byte[] in = response.readEntity(byte[].class);
            out.write(in);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String testSoknadJson() {
        return "{\"arbeidsforhold\": {}, \"barn\": {}, \"barnehageplass\": {}, \"familieforhold\": {}, \"sokerKrav\": {}}";
    }

}