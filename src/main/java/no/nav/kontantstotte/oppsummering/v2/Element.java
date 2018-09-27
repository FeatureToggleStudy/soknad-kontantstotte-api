package no.nav.kontantstotte.oppsummering.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Element {

    public static Element nyttSvar(String sporsmal, String svar){
        Element element = new Element();
        element.sporsmal = sporsmal;
        element.svar = svar;
        return element;
    }

    @JsonProperty("sporsmal")
    public String sporsmal;

    @JsonProperty("svar")
    public String svar;

    @JsonProperty("underelementer")
    public List<Element> underelementer;

}