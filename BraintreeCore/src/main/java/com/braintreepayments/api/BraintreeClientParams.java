package com.braintreepayments.api;

import android.content.Context;

import com.braintreepayments.api.internal.BraintreeGraphQLHttpClient;
import com.braintreepayments.api.internal.BraintreeHttpClient;
import com.braintreepayments.api.internal.ManifestValidator;
import com.braintreepayments.api.models.Authorization;

class BraintreeClientParams {

    private Authorization authorization;
    private AnalyticsClient analyticsClient;
    private BraintreeHttpClient httpClient;
    private Context context;

    private String sessionId;
    private String integrationType;
    private BraintreeGraphQLHttpClient graphQLHttpClient;

    private ConfigurationManager configurationManager;
    private BrowserSwitchClient browserSwitchClient;
    private ManifestValidator manifestValidator;

    Authorization getAuthorization() {
        return authorization;
    }

    BraintreeClientParams authorization(Authorization authorization) {
        this.authorization = authorization;
        return this;
    }

    AnalyticsClient getAnalyticsClient() {
        return analyticsClient;
    }

    BraintreeClientParams analyticsClient(AnalyticsClient analyticsClient) {
        this.analyticsClient = analyticsClient;
        return this;
    }

    BraintreeHttpClient getHttpClient() {
        return httpClient;
    }

    BraintreeClientParams httpClient(BraintreeHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    Context getContext() {
        return context;
    }

    BraintreeClientParams context(Context context) {
        this.context = context;
        return this;
    }

    String getSessionId() {
        return sessionId;
    }

    BraintreeClientParams sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    BraintreeGraphQLHttpClient getGraphQLHttpClient() {
        return graphQLHttpClient;
    }

    BraintreeClientParams graphQLHttpClient(BraintreeGraphQLHttpClient graphQLHttpClient) {
        this.graphQLHttpClient = graphQLHttpClient;
        return this;
    }

    ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    BraintreeClientParams configurationManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        return this;
    }

    BrowserSwitchClient getBrowserSwitchClient() {
        return browserSwitchClient;
    }

    BraintreeClientParams browserSwitchClient(BrowserSwitchClient browserSwitchClient) {
        this.browserSwitchClient = browserSwitchClient;
        return this;
    }

    ManifestValidator getManifestValidator() {
        return manifestValidator;
    }

    BraintreeClientParams manifestValidator(ManifestValidator manifestValidator) {
        this.manifestValidator = manifestValidator;
        return this;
    }

    public String getIntegrationType() {
        return integrationType;
    }

    public BraintreeClientParams setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
        return this;
    }
}