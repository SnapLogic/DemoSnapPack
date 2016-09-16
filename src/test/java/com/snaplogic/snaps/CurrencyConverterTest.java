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

import com.google.common.base.Supplier;
import com.google.inject.AbstractModule;
import com.snaplogic.snaps.CurrencyConverter.ForEx;
import com.snaplogic.snap.test.harness.SnapTestRunner;
import com.snaplogic.snap.test.harness.TestFixture;
import com.snaplogic.snap.test.harness.TestResult;
import com.snaplogic.snap.test.harness.TestSetup;

import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;

@RunWith(SnapTestRunner.class)
public class CurrencyConverterTest {

    @TestFixture(snap = CurrencyConverter.class,
            input = "data/currency_converter/input_documents.json",
            outputs = "output0",
            expectedOutputPath = "data/currency_converter")
    public void currencyConverstion_WithFieldMockedDirectly_ConvertsCorrectly(TestSetup testSetup)
            throws Exception {
        // This demonstrates how to inject mocks into Snap fields.
        ForEx forExMock = createMock(ForEx.class);
        expect(forExMock.getExchangeRates(anyString()))
                .andReturn(new FakeForeignExchange().getExchangeRates("USD")).anyTimes();
        replay(forExMock);

        testSetup.inject().fieldName("foreignExchange").dependency(forExMock).add();

        TestResult testResult = testSetup.test();
        assertNull(testResult.getException());

        verify(forExMock);
    }

    @TestFixture(snap = CurrencyConverter.class,
            input = "data/currency_converter/input_documents.json",
            outputs = "output0",
            expectedOutputPath = "data/currency_converter",
            injectorModule = FakeForeignExchangeInjector.class)
    public void currencyConversion_WithCustomInjector_ConvertsCorrectly() throws Exception {

    }

    @TestFixture(snap = CurrencyConverter.class,
            input = "data/currency_converter/input_documents.json",
            outputs = "output0",
            expectedOutputPath = "data/currency_converter",
            properties = "data/currency_converter/exchange_rates_from_file_properties.json",
            dataFiles = {"data/currency_converter/exchange_rates_file.json"})
    public void currencyConversion_WithExchangeRatesFromDataFiles_ConvertsCorrectly()
            throws Exception {

    }

    @TestFixture(snap = CurrencyConverter.class,
            input = "data/currency_converter/input_documents.json",
            outputs = "output0",
            expectedOutputPath = "data/currency_converter",
            properties = "data/currency_converter/exchange_rates_from_file_properties.json",
            dataFilesSupplier = ExchangeRatesSupplier.class)
    public void currencyConversion_WithExchangeRatesDataSupplier_ConvertsCorrectly()
            throws Exception {

    }

    public static class FakeForeignExchangeInjector extends AbstractModule {
        FakeForeignExchange fakeForeignExchange = new FakeForeignExchange();

        @Override
        protected void configure() {
            bind(ForEx.class).toInstance(fakeForeignExchange);
        }
    }

    public static class FakeForeignExchange implements ForEx {
        private Map<String, Object> rate;

        public FakeForeignExchange() {
            rate = new LinkedHashMap<>();
        }

        @Override
        public Map<String, Object> getExchangeRates(String currencyCode) {
            Map<String, Object> rate = new LinkedHashMap<>();
            rate.put("base", "USD");
            rate.put("date", "2016-09-12");

            Map<String, Object> forExRates = new LinkedHashMap<>();
            forExRates.put("AUD", BigDecimal.valueOf(1.3299));
            forExRates.put("GBP", BigDecimal.valueOf(0.75249));
            forExRates.put("EUR", BigDecimal.valueOf(0.89079));
            rate.put("rates", forExRates);

            return rate;
        }
    }

    public static class ExchangeRatesSupplier implements Supplier<String[]> {
        @Override
        public String[] get() {
            return new String[]{
                    "data/currency_converter/exchange_rates_file.json"
            };
        }
    }
}
