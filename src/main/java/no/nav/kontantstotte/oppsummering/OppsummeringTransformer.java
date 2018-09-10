package no.nav.kontantstotte.oppsummering;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

public class OppsummeringTransformer {

    private NashornScriptEngine engineHolder;

    public OppsummeringTransformer() {
        this.engineHolder = getandInitializeEngineHolder();
    }

    private NashornScriptEngine getandInitializeEngineHolder() {
        NashornScriptEngine nashornScriptEngine = (NashornScriptEngine)
                new ScriptEngineManager().getEngineByName("nashorn");
        try {
            nashornScriptEngine.eval(read("static/polyfills/nashorn-polyfill.js"));
            nashornScriptEngine.eval(read("react-pdf/node_modules/react/umd/react.production.min.js"));
            nashornScriptEngine.eval(read("react-pdf/node_modules/react-dom/umd/react-dom-server.browser.production.min.js"));

            evaluerPdfKomponenter(nashornScriptEngine);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return nashornScriptEngine;
    }

    private void evaluerPdfKomponenter(NashornScriptEngine nashorn) throws ScriptException {
        nashorn.eval(read("react-pdf/dist/bundle.js"));
    }

    private Reader read(String path) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(path);
        return new InputStreamReader(in);
    }

    public String renderHTMLForPdf(Soknad soknad) {
        try {
            Properties prop = new Properties();
            prop.load(
                new InputStreamReader(
                        new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/tekster.properties"),
                        Charset.forName("UTF-8")
                )
            );

            Object html = engineHolder.invokeFunction("hentHTMLStringForOppsummering", soknad, prop);
            return String.valueOf(html);
        } catch (ScriptException | NoSuchMethodException | IOException e) {
            throw new IllegalStateException("Klarte ikke rendre react-komponent", e);
        }
    }
}
