package com.tw.go.plugin.maven.nexus;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.maven.client.HtmlResponseParser;
import com.tw.go.plugin.maven.client.RepoResponse;
import maven.MavenVersion;

import java.util.ArrayList;
import java.util.List;

public class NexusResponseHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(NexusResponseHandler.class);
    private static final String FILES_TO_IGNORE = "^maven-metadata.*$|^archetype-catalog.*$|^.*sha1$|^.*md5$|^.*pom$";
    private final RepoResponse repoResponse;
    private Content content;

    public NexusResponseHandler(RepoResponse repoResponse) {
        this.repoResponse = repoResponse;
    }

    public boolean canHandle() {
        if (repoResponse.isXml()) {
            LOGGER.info("NexusResponseHandler unmarshalling XML answer");
            content = new Content().unmarshal(repoResponse.getResponseBody());
        }
        if (repoResponse.isHtml()) {
            LOGGER.info("NexusResponseHandler parsing HTML answer");
            content = HtmlResponseParser.parse(repoResponse.getResponseBody());
        }
        if (content == null) {
            LOGGER.warn("NexusResponseHandler can't handle: " + repoResponse.getMimeType());
        }
        return content != null;
    }

    public List<MavenVersion> getAllVersions() {
        if(content == null && !canHandle()){
            LOGGER.warn("NexusResponseHandler getAllVersions invalidContent");
            throw new RuntimeException("getAllVersions: Invalid response");
        }
        List<MavenVersion> versions = new ArrayList<MavenVersion>();
        for (ContentItem ci : content.getContentItems()) {
            if (!ci.getText().matches(FILES_TO_IGNORE)) {
                MavenVersion version = ci.toVersion();
                versions.add(version);
            }
        }
        return versions;
    }

    public String getPOMurl() {
        return getFilesMatching(".*\\.pom$").get(0);
    }

    public List<String> getFilesMatching(String artifactSelectionPattern) {
        if(content == null && !canHandle())
            throw new RuntimeException("getFilesMatching: Invalid response");
        List<String> files = new ArrayList<String>();
        for (ContentItem ci : content.getContentItems()) {
            if (ci.getText().matches(artifactSelectionPattern))
                files.add(ci.getText());
        }
        return files;
    }
}
