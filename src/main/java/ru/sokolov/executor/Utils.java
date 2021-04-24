package ru.sokolov.executor;

import com.google.caja.SomethingWidgyHappenedError;
import com.google.caja.lexer.*;
import com.google.caja.parser.js.Block;
import com.google.caja.parser.js.Parser;
import com.google.caja.reporting.EchoingMessageQueue;
import com.google.caja.reporting.MessageContext;
import com.google.caja.reporting.MessageQueue;
import com.google.caja.util.ContentType;
import com.google.caja.util.Criterion;
import com.google.common.io.Resources;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class Utils {
    static MessageContext mc = new MessageContext();
    static MessageQueue mq = new EchoingMessageQueue(
            new PrintWriter(new OutputStreamWriter(System.err)), mc, false);

    public static URI getResource(String resource) {
        URL url = Resources.getResource(resource);
        try {
            return null != url ? url.toURI() : null;
        } catch (URISyntaxException ex) {
            throw new SomethingWidgyHappenedError(
                    "The following url is not a valid uri: " + url);
        }
    }

    public static InputStream getResourceAsStream(String resource)
            throws IOException {
        URI uri = getResource(resource);
        if (null == uri) {
            throw new FileNotFoundException(
                    "Resource " + resource);
        }
        URLConnection conn = uri.toURL().openConnection();
        conn.connect();
        return conn.getInputStream();
    }

    protected static FetchedData dataFromResource(String resourcePath, InputSource is)
            throws IOException {
        ContentType guess = GuessContentType.guess(null, resourcePath, null);
        return FetchedData.fromStream(
                getResourceAsStream(resourcePath),
                guess != null ? guess.mimeType : "", "UTF-8", is);
    }

    protected static CharProducer fromResource(String resourcePath, InputSource is)
            throws IOException {
        return dataFromResource(resourcePath, is).getTextualContent();
    }

    protected static CharProducer fromResource(String resourcePath)
            throws IOException {
        URI resource = getResource(resourcePath);
        if (resource == null) {
            throw new FileNotFoundException(resourcePath);
        }
        return fromResource(resourcePath, new InputSource(resource));
    }

    protected static Block js(CharProducer cp) throws ParseException {
        return js(cp, false);
    }

    protected static Block js(CharProducer cp, boolean quasi) throws ParseException {
        return js(cp, JsTokenQueue.NO_COMMENT, quasi);
    }

    private static InputSource sourceOf(CharProducer cp) {
        return cp.getSourceBreaks(0).source();
    }

    protected static Block js(
            CharProducer cp, Criterion<Token<JsTokenType>> filt, boolean quasi)
            throws ParseException {
        JsLexer lexer = new JsLexer(cp);
        JsTokenQueue tq = new JsTokenQueue(lexer, sourceOf(cp), filt);
        Parser p = new Parser(tq, mq, quasi);
        Block b = p.parse();
        tq.expectEmpty();
        return b;
    }
}
