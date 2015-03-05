package com.tw.go.plugin.maven.client;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.maven.config.LookupParams;
import com.tw.go.plugin.maven.nexus.NexusResponseHandler;
import com.tw.go.plugin.util.HttpRepoURL;

import maven.MavenVersion;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RepositoryClientTest {
	// Sat Jan 13 01:28:39 EST 2007 "EEE MMM d HH:mm:ss zzz yyyy"
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", new Locale("US"));
	
    @Test
    public void shouldGetLatestVersion() throws IOException {
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, null);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(new RepoResponse(responseBody, RepoResponse.APPLICATION_XML),DATE_FORMAT);
        MavenVersion result = client.getLatest(nexusResponseHandler.getAllVersions());
        assertThat(result.getVersion(), is("1.0.0"));
        assertThat(result.getQualifier(), is("18"));
    }

    @Test
    public void shouldReturnNullIfNoNewerVersion() throws IOException {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.0-18", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.0-18");
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(new RepoResponse(responseBody, RepoResponse.APPLICATION_XML), DATE_FORMAT);
        assertNull(client.getLatest(nexusResponseHandler.getAllVersions()));
    }

    @Test
    public void shouldReturnNewerVersion() throws IOException {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.0-17", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.0-17");
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", null, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        String responseBody = FileUtils.readFileToString(new File("test/fast/nexus-response.xml"));
        NexusResponseHandler nexusResponseHandler = new NexusResponseHandler(new RepoResponse(responseBody, RepoResponse.APPLICATION_XML),DATE_FORMAT);
        MavenVersion result = client.getLatest(nexusResponseHandler.getAllVersions());
        assertThat(result.getVersion(), is("1.0.0"));
        assertThat(result.getQualifier(), is("18"));
    }

    @Test
    public void shouldHonorUpperBound() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.14");
        String upperBound = "1.0.17";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", null, upperBound, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.18"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorLowerBoundWithKnownPreviousVersion() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.14");
        String lowerBound = "0.1";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", lowerBound, null, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.18"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.12"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("0.0.12"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorLowerBound() {
        String lowerBound = "0.1";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", lowerBound, null, null);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.18"));
        allVersions.add(new MavenVersion("1.0.16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.18"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.12"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.12"));
        allVersions.clear();
        allVersions.add(new MavenVersion("0.0.12"));
        assertNull(client.getLatest(allVersions));
    }

    @Test
    public void shouldHonorUpperBoundAtQualifierLevel() {
        PackageRevision previouslyKnownRevision = new PackageRevision("1.0.0-14", new Date(), "abc");
        previouslyKnownRevision.addData(LookupParams.PACKAGE_VERSION, "1.0.0-14");
        String upperBound = "1.0.0-17";
        LookupParams lookupParams = new LookupParams(
                new HttpRepoURL("http://nexus-server:8081/nexus/content/repositories/releases/", null, null),
                DATE_FORMAT,
                "com.thoughtworks.studios.go", "book_inventory", "war", null, upperBound, previouslyKnownRevision);
        RepositoryClient client = new RepositoryClient(lookupParams);
        List<MavenVersion> allVersions = new ArrayList<MavenVersion>();
        allVersions.add(new MavenVersion("1.0.0-18"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-18"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertNull(client.getLatest(allVersions));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-16"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-15"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-14"));
        allVersions.add(new MavenVersion("1.0.0-16"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertThat(client.getLatest(allVersions).getV_Q(), is("1.0.0-16"));
        allVersions.clear();
        allVersions.add(new MavenVersion("1.0.0-14"));
        allVersions.add(new MavenVersion("1.0.0-17"));
        assertNull(client.getLatest(allVersions));
    }
}
