package no.nav.kontantstotte.innsending;

import no.nav.kontantstotte.storage.Storage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.kontantstotte.config.toggle.FeatureToggleConfig.KONTANTSTOTTE_VEDLEGG;
import static no.nav.kontantstotte.config.toggle.UnleashProvider.toggle;
import static no.nav.kontantstotte.innlogging.InnloggingUtils.hentFnrFraToken;

class VedleggProvider {

    private final Storage storage;

    VedleggProvider(Storage storage) {
        this.storage = storage;
    }

    List<VedleggDto> hentVedleggFor(Soknad soknad) {

        if (toggle(KONTANTSTOTTE_VEDLEGG).isDisabled()) {
            return null;
        }

        String directory = hentFnrFraToken();

        List<List<VedleggMetadata>> alleVedlegg = Arrays.asList(
                soknad.barnehageplass.harSluttetIBarnehageVedlegg,
                soknad.barnehageplass.skalSlutteIBarnehageVedlegg
        );

        return alleVedlegg.stream()
                .flatMap(List::stream)
                .map(v -> new VedleggDto(
                        storage.get(directory, v.getFilreferanse())
                                .orElseThrow(() -> new InnsendingException("Foresøker å sende inn en søknad med vedlegg som ikke finnes " + v.getFilreferanse())),
                        v.getFilnavn()))
                .collect(Collectors.toList());
    }
}
