package no.nav.kontantstotte.innsending.oppsummering.html.mapping;

import no.nav.kontantstotte.innsending.Soknad;
import no.nav.kontantstotte.innsending.steg.Familieforhold;
import no.nav.kontantstotte.innsending.oppsummering.html.Bolk;

import java.util.ArrayList;

import static no.nav.kontantstotte.innsending.oppsummering.html.mapping.Tekstnokkel.*;

public class FamilieforholdMapping extends BolkMapping {
    public FamilieforholdMapping(Tekster tekster) {
        super(tekster);
    }

    @Override
    public Bolk map(Soknad soknad) {
        Bolk bolk = new Bolk();

        this.setBrukFlertall( "JA".equalsIgnoreCase(soknad.mineBarn.erFlerling));

        Familieforhold familieforhold = soknad.familieforhold;
        bolk.tittel = tekster.hentTekst(FAMILIEFORHOLD_TITTEL.getNokkel());
        bolk.elementer = new ArrayList<>();

        if("NEI".equalsIgnoreCase(familieforhold.borForeldreneSammenMedBarnet)){
            bolk.elementer.add(nyttElementMedTekstsvar.apply(FAMILIEFORHOLD_BOR_SAMMEN, SVAR_NEI));
        }
        if("JA".equalsIgnoreCase(familieforhold.borForeldreneSammenMedBarnet)){
            bolk.elementer.add(nyttElementMedTekstsvar.apply(FAMILIEFORHOLD_BOR_SAMMEN, SVAR_JA));
            bolk.elementer.add(nyttElementMedVerdisvar.apply(FAMILIEFORHOLD_NAVN_ANNEN_FORELDER, familieforhold.annenForelderNavn));
            bolk.elementer.add(nyttElementMedVerdisvar.apply(FAMILIEFORHOLD_FNR_ANNEN_FORELDER, familieforhold.annenForelderFodselsnummer));
        }
        return bolk;
    }
}
