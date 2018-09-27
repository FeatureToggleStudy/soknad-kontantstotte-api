package no.nav.kontantstotte.oppsummering;

import no.nav.kontantstotte.oppsummering.v1.bolk.Barn;
import no.nav.kontantstotte.oppsummering.v2.Bolk;
import no.nav.kontantstotte.oppsummering.v2.Element;
import no.nav.kontantstotte.oppsummering.v2.SoknadTilOppsummering;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;


public class SoknadTilOppsummeringTest {

    @Test
    public void tilBarneBolk() {
        String tittel = "BARN";
        String undertittel = "Barn du søker kontantstøtte for";
        String navn = "Navn";
        String fodselsdato = "Fødselsdato";

        Map<String, String> tekster = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>(SoknadTilOppsummering.BARN_TITTEL, tittel),
                new AbstractMap.SimpleEntry<>(SoknadTilOppsummering.BARN_UNDERTITTEL, undertittel),
                new AbstractMap.SimpleEntry<>(SoknadTilOppsummering.BARN_NAVN, navn),
                new AbstractMap.SimpleEntry<>(SoknadTilOppsummering.BARN_FODSELSDATO, fodselsdato))
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        Barn innsendtBarn = new Barn();
        innsendtBarn.navn = "Barnets navn";
        innsendtBarn.fodselsdato = "01.01.2019";
        Bolk bolk = new SoknadTilOppsummering().mapBarn(innsendtBarn, tekster);
        assertThat(bolk)
                .extracting("tittel", "undertittel")
                .containsExactly(tittel, undertittel);

        List<Element> elementer = bolk.elementer;
        assertThat(elementer)
                .extracting("sporsmal", "svar")
                .contains(
                        tuple(navn, innsendtBarn.navn),
                        tuple(fodselsdato, innsendtBarn.fodselsdato));


    }
}