package no.nav.kontantstotte.oppsummering.innsending.v2.mapping;


import no.nav.kontantstotte.oppsummering.Soknad;

import java.util.List;
import java.util.Map;

public class SoknadOppsummering {

    private Soknad soknad;
    private String fnr;
    private String innsendingsTidspunkt;
    private List<Bolk> bolker;
    private Map<String, String> tekster;

    public SoknadOppsummering(){

    }

    public SoknadOppsummering(Soknad soknad, String fnr, String innsendingsTidspunkt, List<Bolk> bolker, Map<String, String> tekster) {
        this.soknad = soknad;
        this.fnr = fnr;
        this.innsendingsTidspunkt = innsendingsTidspunkt;
        this.bolker = bolker;
        this.tekster = tekster;
    }

    public Soknad getSoknad() {
        return soknad;
    }

    public String getInnsendingsTidspunkt() {
        return innsendingsTidspunkt;
    }

    public String getFnr() {
        return fnr;
    }

    public List<Bolk> getBolker() {
        return bolker;
    }

    public Map<String, String> getTekster() {
        return tekster;
    }
}
