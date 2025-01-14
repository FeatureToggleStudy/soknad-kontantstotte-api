package no.nav.kontantstotte.innsyn.service.rest;

import static no.nav.kontantstotte.innsyn.service.rest.InnsynConverter.personinfoDtoToPerson;
import static no.nav.kontantstotte.innsyn.service.rest.InnsynConverter.relasjonDtoToBarn;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import no.nav.familie.http.client.NavHttpHeaders;
import no.nav.familie.log.mdc.MDCConstants;
import no.nav.kontantstotte.logging.TjenesteLogger;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.kontantstotte.client.HttpClientUtil;
import no.nav.kontantstotte.client.TokenHelper;
import no.nav.kontantstotte.innsyn.domain.Barn;
import no.nav.kontantstotte.innsyn.domain.InnsynOppslagException;
import no.nav.kontantstotte.innsyn.domain.InnsynService;
import no.nav.kontantstotte.innsyn.domain.Person;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import no.nav.tps.innsyn.PersoninfoDto;
import no.nav.tps.innsyn.RelasjonDto;

@Profile("!mockgen-tps")
@Component
class InnsynServiceClient implements InnsynService {

    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private static final Logger logger = LoggerFactory.getLogger(InnsynServiceClient.class);
    private static final String CONSUMER_ID = "soknad-kontantstotte-api";

    private static final Integer MIN_ALDER_I_MANEDER = 10;

    private static final Integer MAKS_ALDER_I_MANEDER = 28;
    private final Counter sokerErIkkeKvalifisert = Metrics.counter("soknad.kontantstotte.kvalifisert", "status", "NEI");
    private final Counter sokerErKvalifisert = Metrics.counter("soknad.kontantstotte.kvalifisert", "status", "JA");
    private final Timer tpsResponstid = Metrics.timer("tps.respons.tid");
    private final HttpClient client;
    private URI tpsInnsynServiceUri;
    private ObjectMapper mapper;
    private String tpsProxyApiKeyUsername;
    private String tpsProxyApiKeyPassword;
    private OIDCRequestContextHolder contextHolder;

    @Autowired
    InnsynServiceClient(@Value("${TPS-PROXY_API_V1_INNSYN_URL}") URI tpsInnsynServiceUri,
                        @Value("${SOKNAD-KONTANTSTOTTE-API-TPS-PROXY_API_V1_INNSYN-APIKEY_USERNAME}") String tpsProxyApiKeyUsername,
                        @Value("${SOKNAD-KONTANTSTOTTE-API-TPS-PROXY_API_V1_INNSYN-APIKEY_PASSWORD}") String tpsProxyApiKeyPassword,
                        OIDCRequestContextHolder contextHolder,
                        ObjectMapper mapper) {
        this.tpsProxyApiKeyUsername = tpsProxyApiKeyUsername;
        this.tpsProxyApiKeyPassword = tpsProxyApiKeyPassword;
        this.contextHolder = contextHolder;
        this.tpsInnsynServiceUri = tpsInnsynServiceUri;
        this.mapper = mapper;
        this.client = HttpClientUtil.create();
    }

    static boolean erIKontantstotteAlder(String fødselsdato) {
        Period diff = Period.between(LocalDate.parse(fødselsdato), LocalDate.now());
        Integer alderIManeder = diff.getYears() * 12 + diff.getMonths();
        return (alderIManeder >= MIN_ALDER_I_MANEDER) &&
                (alderIManeder <= MAKS_ALDER_I_MANEDER) &&
                !(alderIManeder.equals(MAKS_ALDER_I_MANEDER) && diff.getDays() > 0);
    }

    @Override
    public Person hentPersonInfo(String fnr) {
        long startTime = System.nanoTime();
        PersoninfoDto dto = hentInnsynsRespons("person", fnr, PersoninfoDto.class);
        tpsResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

        return personinfoDtoToPerson.apply(dto);
    }

    @Override
    public List<Barn> hentBarnInfo(String fnr) {
        long startTime = System.nanoTime();
        List<RelasjonDto> dtoList = hentInnsynsResponsListe("barn", fnr, RelasjonDto.class);
        tpsResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);

        List<Barn> returListe = dtoList
                .stream()
                .filter(dto -> erIKontantstotteAlder(dto.getFoedselsdato()))
                .filter(RelasjonDto::isHarSammeAdresse)
                .map(dto -> relasjonDtoToBarn.apply(dto))
                .collect(Collectors.toList());

        if (returListe.isEmpty()) {
            sokerErIkkeKvalifisert.increment();
        } else {
            sokerErKvalifisert.increment();
        }

        return returListe;
    }

    @Override
    public void ping() {
        HttpRequest request = HttpClientUtil.createRequest()
                .uri(URI.create(tpsInnsynServiceUri + "/internal/alive"))
                .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), CONSUMER_ID)
                .header(tpsProxyApiKeyUsername, tpsProxyApiKeyPassword)
                .GET()
                .build();

        HttpStatus.Series status;
        try {
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            status = HttpStatus.Series.resolve(response.statusCode());
        } catch (IOException | InterruptedException e) {
            status = HttpStatus.Series.SERVER_ERROR;
        }

        if (!HttpStatus.Series.SUCCESSFUL.equals(status)) {
            throw new InnsynOppslagException("TPS innsyn service is not up");
        }
    }

    @Override
    public String toString() {
        return "InnsynServiceClient{" +
                "client=" + client +
                ", tpsInnsynServiceUri=" + tpsInnsynServiceUri +
                '}';
    }

    private <T> T hentInnsynsRespons(String path, String fnr, Class<T> dtoClass) {
        HttpResponse<String> response = getInnsynResponse(path, fnr);
        try {
            T data = mapper.readValue(response.body(), dtoClass);
            TjenesteLogger.logTjenestekall(URI.create(tpsInnsynServiceUri + path), fnr, data);

            return data;
        } catch (IOException e) {
            throw new InnsynOppslagException("TPS innsyn service is not up");
        } catch (Exception e) {
            TjenesteLogger.logFeil(URI.create(tpsInnsynServiceUri + path), fnr, e);
            throw e;
        }
    }

    private <T> List<T> hentInnsynsResponsListe(String path, String fnr, Class<T> dtoClass) {
        HttpResponse<String> response = getInnsynResponse(path, fnr);
        try {
            CollectionType typeReference =
                    TypeFactory.defaultInstance().constructCollectionType(List.class, dtoClass);

            List<T> data = mapper.readValue(response.body(), typeReference);
            TjenesteLogger.logTjenestekall(URI.create(tpsInnsynServiceUri + path), fnr, data);

            return data;
        } catch (IOException e) {
            throw new InnsynOppslagException("TPS innsyn service is not up");
        } catch (Exception e) {
            TjenesteLogger.logFeil(URI.create(tpsInnsynServiceUri + path), fnr, e);
            throw e;
        }
    }

    private HttpResponse<String> getInnsynResponse(String path, String fnr) {
        URI uri = URI.create(tpsInnsynServiceUri + path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(HttpHeader.AUTHORIZATION.asString(), TokenHelper.generateAuthorizationHeaderValueForLoggedInUser(contextHolder))
                .header(NavHttpHeaders.NAV_CALLID.asString(), MDC.get(MDCConstants.MDC_CALL_ID))
                .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), CONSUMER_ID)
                .header(tpsProxyApiKeyUsername, tpsProxyApiKeyPassword)
                .header(NavHttpHeaders.NAV_PERSONIDENT.asString(), fnr)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (!HttpStatus.Series.SUCCESSFUL.equals(HttpStatus.Series.resolve(response.statusCode()))) {
                logger.info("Kall mot innsynstjeneste feilet: " + response.body());
                throw new InnsynOppslagException(response.body());
            } else {
                return response;
            }
        } catch (IOException | InterruptedException e) {
            logger.info("Ukjent feil ved oppslag mot '" + uri + "'. " + e.getMessage());
            throw new InnsynOppslagException("Ukjent feil ved oppslag mot '" + uri + "'. " + e.getMessage());
        } catch (Exception e) {
            TjenesteLogger.logFeil(URI.create(tpsInnsynServiceUri + path), fnr, e);
            throw e;
        }
    }
}
