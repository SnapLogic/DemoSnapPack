/*
 * SnapLogic - Data Integration
 *
 * Copyright (C) 2016, SnapLogic, Inc.  All rights reserved.
 *
 * This program is licensed under the terms of
 * the SnapLogic Commercial Subscription agreement.
 *
 * "SnapLogic" is a trademark of SnapLogic, Inc.
 */
package com.snaplogic.snaps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.DependencyManager;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import com.snaplogic.snap.api.fs.JfsUtils;
import com.snaplogic.snap.api.fs.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;

/**
 * A basic example of a Snap that converts amount values from USD to another currency.
 *
 * <p>{@link DependencyManager} is used to inject dependencies for {@link ForEx} and
 * {@link ObjectMapper}.</p>
 */
@General(title = "Currency Converter", author = "Your Company Name",
        purpose = "Demonstrates dependency injection",
        docLink = "http://yourdocslinkhere.com")
@Inputs(min = 1, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
@Version(snap = 1)
@Category(snap = SnapCategory.READ)
public class CurrencyConverter extends SimpleSnap implements DependencyManager {

    private static final String INPUT_FILE_PROP = "inputFile";
    private static final String REGEX_PATTERN_PROTOCOL = "^sldb:///|^http://|^https://|^file:///";
    private static final Pattern PATTERN = Pattern.compile(REGEX_PATTERN_PROTOCOL);

    private String filePath;
    private TypeReference<Map<String, Object>> mapTypeReference =
            new TypeReference<Map<String, Object>>() {
            };

    // the SnapLogic platform takes care of injecting an ObjectMapper instance
    @Inject
    private ObjectMapper mapper;
    @Inject
    private ForEx foreignExchange;
    @Inject
    private URLEncoder urlEncoder;
    @Inject
    private JfsUtils jfsUtils;

    // Use an instance of Guice's AbstractModule to bind implementations to interfaces
    @Override
    public Module getManagedModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(ForEx.class).to(ForExImpl.class);
                bind(JfsUtils.class).toInstance(JfsUtils.getInstance());
            }
        };
    }

    // An optional file selector for providing exchange rates within a file
    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
        propertyBuilder.describe(INPUT_FILE_PROP, "Exchange Rates File",
                "File containing foreign exchange rates")
                .expression()
                .fileBrowsing()
                .schemaAware(SnapProperty.DecoratorType.ACCEPTS_SCHEMA)
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        filePath = propertyValues.getAsExpression(INPUT_FILE_PROP).eval(null);
    }

    @Override
    protected void process(Document document, String inputViewName) {
        @SuppressWarnings("unchecked")
        Map<String, Object> docAsMap = documentUtility.getAsMap(document, errorViews);
        String targetCurrency = (String) docAsMap.get("to");
        BigDecimal amount = BigDecimal.valueOf(((Number) docAsMap.get("amount")).doubleValue());

        Map<String, Object> exchangeRates = null;

        // If a file was provided, use the exchange rates within it; otherwise, look them up from
        // the service
        if (StringUtils.isNotBlank(filePath)) {
            exchangeRates = getExchangeRatesFromFile(document);
        } else {
            exchangeRates = foreignExchange.getExchangeRates(targetCurrency);
        }

        if (exchangeRates != null) {
            outputViews.write(documentUtility.newDocument(
                    getExchangeRateForCurrency(targetCurrency, amount, exchangeRates)), document);
        }
    }

    private Map<String, Object> getExchangeRatesFromFile(Document document) {
        Map<String, Object> exchangeRates;
        InputStream inputStream = null;
        try {
            URI filePathUri = urlEncoder.validateAndEncodeURI(filePath, PATTERN, null);
            inputStream = jfsUtils.openURLConnection(filePathUri).getInputStream();
            exchangeRates = mapper.readValue(inputStream, mapTypeReference);
        } catch (IOException e) {
            throw new SnapDataException(document, e,
                    String.format("Unable to read from file path %s", filePath));
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return exchangeRates;
    }

    private Map<String, BigDecimal> getExchangeRateForCurrency(String targetCurrency,
            BigDecimal amount, Map<String, Object> rate) {
        Map<String, Object> forExRates = (Map<String, Object>) rate.get("rates");
        BigDecimal forExRate = BigDecimal.valueOf(
                ((Number) forExRates.get(targetCurrency)).doubleValue());

        Map<String, BigDecimal> convertedCurrency = new LinkedHashMap<>();
        convertedCurrency.put(targetCurrency, amount.multiply(forExRate));
        return convertedCurrency;
    }

    public interface ForEx {
        Map<String, Object> getExchangeRates(String currencyCode);
    }

    // An implementation of the ForEx interface. Normally this would call out to a database or
    // web service; in this demonstration, it returns random exchange rates.
    public static class ForExImpl implements ForEx {
        private Map<String, Object> rate;

        public ForExImpl() {
            rate = new LinkedHashMap<>();
        }

        @Override
        public Map<String, Object> getExchangeRates(String currencyCode) {
            rate.put("base", currencyCode.toUpperCase());
            rate.put("date", DateTimeFormat.forPattern("yyyy-MM-dd").print(now(UTC)));

            Map<String, BigDecimal> forExRates = new LinkedHashMap<>();
            Random r = new Random();
            forExRates.put("AUD", BigDecimal.valueOf(10 * r.nextDouble()));
            forExRates.put("GBP", BigDecimal.valueOf(10 * r.nextDouble()));
            forExRates.put("EUR", BigDecimal.valueOf(10 * r.nextDouble()));
            rate.put("rates", forExRates);

            return rate;
        }
    }
}
