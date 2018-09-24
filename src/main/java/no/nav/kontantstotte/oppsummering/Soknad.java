package no.nav.kontantstotte.oppsummering;

import no.nav.kontantstotte.oppsummering.bolk.*;

import java.time.LocalDateTime;

public class Soknad {

    public LocalDateTime innsendingTimestamp;
    public Person person;
    public SokerKrav kravTilSoker;
    public Familieforhold familieforhold;
    public Barnehageplass barnehageplass;
    public ArbeidIUtlandet arbeidIUtlandet;
    public UtenlandskKontantstotte utenlandskKontantstotte;
    public Barn mineBarn;
    public UtenlandskeYtelser utenlandskeYtelser;
    public Oppsummering oppsummering;
    public String sprak;

    public Soknad() {
        this.person = new Person();
        this.kravTilSoker = new SokerKrav();
        this.familieforhold = new Familieforhold();
        this.barnehageplass = new Barnehageplass();
        this.arbeidIUtlandet = new ArbeidIUtlandet();
        this.utenlandskeYtelser = new UtenlandskeYtelser();
        this.oppsummering = new Oppsummering();
        this.utenlandskKontantstotte = new UtenlandskKontantstotte();
        this.mineBarn = new Barn();
    }

    public boolean erGyldig() {
        return this.oppsummering.erGyldig();
    }
}
