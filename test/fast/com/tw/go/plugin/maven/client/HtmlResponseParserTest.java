package com.tw.go.plugin.maven.client;

import com.tw.go.plugin.maven.nexus.Content;
import com.tw.go.plugin.maven.nexus.ContentItem;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class HtmlResponseParserTest {

    @Test
    public void shouldParseHtmlDir() throws IOException {
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-antlr-dir.html"));
        Content c = HtmlResponseParser.parse(responseBody);
        assertNotNull(c);
        assertEquals(c.getContentItems().size(), 9);
        for(ContentItem item: c.getContentItems()) {
            assertNotNull(item);
        }
    }

    @Test
    public void shouldParseHtmlFiles() throws IOException {
        String responseBody = FileUtils.readFileToString(new File("test/fast/jboss-antlr-files.html"));
        Content c = HtmlResponseParser.parse(responseBody);
        assertNotNull(c);
        assertEquals(c.getContentItems().size(), 6);
        for(ContentItem item: c.getContentItems()) {
            assertNotNull(item);
            assertEquals("true", item.getLeaf());
        }
    }
}
