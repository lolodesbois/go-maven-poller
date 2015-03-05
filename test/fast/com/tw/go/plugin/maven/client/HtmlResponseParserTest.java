package com.tw.go.plugin.maven.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.tw.go.plugin.maven.client.HtmlResponseParser;
import com.tw.go.plugin.maven.nexus.Content;
import com.tw.go.plugin.maven.nexus.ContentItem;

public class HtmlResponseParserTest {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", new Locale("US"));

    @Test
    public void shouldParseHtmlDir() throws IOException {
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-antlr-dir.html"));
        Content c = HtmlResponseParser.parse(responseBody,DATE_FORMAT);
        assertNotNull(c);
        assertEquals(c.getContentItems().size(), 9);
        for(ContentItem item: c.getContentItems()) {
            assertNotNull(item);
        }
    }

    @Test
    public void shouldParseHtmlFiles() throws IOException {
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-antlr-files.html"));
        Content c = HtmlResponseParser.parse(responseBody,DATE_FORMAT);
        assertNotNull(c);
        assertEquals(c.getContentItems().size(), 6);
        for(ContentItem item: c.getContentItems()) {
            assertNotNull(item);
            assertEquals("true", item.getLeaf());
        }
    }
}
