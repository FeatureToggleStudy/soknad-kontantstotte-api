package no.nav.kontantstotte.innsending.oppsummering;

import no.nav.kontantstotte.innsending.Soknad;
import no.nav.kontantstotte.innsending.oppsummering.html.OppsummeringHtmlGenerator;

public class OppsummeringPdfGenerator {
    private final PdfConverter pdfConverter;
    private final OppsummeringHtmlGenerator oppsummeringHtmlGenerator;

    public OppsummeringPdfGenerator(OppsummeringHtmlGenerator oppsummeringHtmlGenerator, PdfConverter pdfConverter) {
        this.oppsummeringHtmlGenerator = oppsummeringHtmlGenerator;
        this.pdfConverter = pdfConverter;
    }

    public byte[] generer(Soknad soknad, String fnr) {
        byte[] htmlBytes = oppsummeringHtmlGenerator.genererHtml(soknad, fnr);
        return pdfConverter.genererPdf(htmlBytes);
    }
}