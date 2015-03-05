package com.tw.go.plugin.maven.config;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;

public class MavenRepoConfig {

	public static final String DATE_FORMAT = "DATE_FORMAT";
	public static final String DATE_LOCALE = "DATE_LOCALE";

    private final RepositoryConfiguration repoConfig;
    private final Property repoUrlProperty;

    public MavenRepoConfig(RepositoryConfiguration repoConfig) {
        this.repoConfig = repoConfig;
        repoUrlProperty = repoConfig.get(RepoUrl.REPO_URL);
    }

    public String stringValueOf(Property packageConfiguration) {
        if (packageConfiguration == null) return null;
        return packageConfiguration.getValue();
    }

    public HttpRepoURL getRepoUrl() {
        RepoUrl repoUrl = RepoUrl.create(
                withTrailingSlash(repoUrlProperty.getValue()),
                stringValueOf(repoConfig.get(RepoUrl.USERNAME)),
                stringValueOf(repoConfig.get(RepoUrl.PASSWORD)));
        if(!repoUrl.isHttp()) throw new RuntimeException("Only http/https urls are supported");
        return (HttpRepoURL) repoUrl;
    }

    public SimpleDateFormat getDateFormat() {
    	return new SimpleDateFormat(repoConfig.get(DATE_FORMAT).getValue(), 
    			                    new Locale(repoConfig.get(DATE_LOCALE).getValue()));
    }
    
    private String withTrailingSlash(String repoUrl) {
        if(repoUrl.endsWith("/")) return repoUrl;
        return repoUrl + "/";
    }

    public boolean isRepoUrlMissing() {
        return repoUrlProperty == null || repoUrlProperty.getValue() == null || repoUrlProperty.getValue().trim().isEmpty();
    }

    public static String[] getValidKeys() {
        return new String[]{RepoUrl.REPO_URL, RepoUrl.USERNAME, RepoUrl.PASSWORD, DATE_FORMAT, DATE_LOCALE};
    }
}
