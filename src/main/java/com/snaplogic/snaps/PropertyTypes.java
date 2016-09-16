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

import com.google.inject.Inject;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.common.SnapType;
import com.snaplogic.common.properties.SnapProperty;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.DocumentUtility;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SimpleSnap;
import com.snaplogic.snap.api.SnapCategory;
import com.snaplogic.snap.api.capabilities.Category;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Inputs;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Snap that demonstrates various property types.
 */
@General(title = "Property Types", purpose = "Demonstrates use of different property types",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Category(snap = SnapCategory.READ)
@Version(snap = 1)
@Inputs(min = 0, max = 1, accepts = {ViewType.DOCUMENT})
@Outputs(min = 1, max = 1, offers = {ViewType.DOCUMENT})
public class PropertyTypes extends SimpleSnap {
    private static final String PASSWORD_PROP = "password_prop";
    private static final String FILE_BROWSER_PROP = "file_browser_prop";
    private static final String CHILD_FILE_BROWSER_PROP = "child_file_browser_prop";
    private static final String CHILD_PROP = "child_prop";
    private static final String PARENT_PROP = "parent_prop";
    private static final String COLUMN_FILE_BROWSER_PROP = "column_file_browser_prop";
    private static final String COLUMN_PROP = "column_prop";
    private static final String TABLE_PROP = "table_prop";
    private static final String PASSWORD_PROP_VALUE = "abc";

    // Document utility is the only way to create a document
    // or manipulate the document header
    @Inject
    private DocumentUtility documentUtility;

    String password;
    String fileBrowser;
    Map<String, Object> composite;
    List<Map<String, Object>> table;

    @Override
    public void defineProperties(final PropertyBuilder propBuilder) {
        // Password (obfuscate)
        propBuilder.describe(PASSWORD_PROP, PASSWORD_PROP)
                .required()
                .defaultValue(PASSWORD_PROP_VALUE)
                .obfuscate() // hides input and sets sensitivity to High
                .add();

        // File Browsing
        propBuilder.describe(FILE_BROWSER_PROP, FILE_BROWSER_PROP)
                .required()
                .defaultValue(PASSWORD_PROP_VALUE)
                .fileBrowsing()
                .add();

        // Composite Property
        SnapProperty fileBrowsingChild = propBuilder.describe(CHILD_FILE_BROWSER_PROP,
                CHILD_FILE_BROWSER_PROP)
                .required()
                .fileBrowsing()
                .build();
        SnapProperty child = propBuilder.describe(CHILD_PROP, CHILD_PROP)
                .required()
                .build();
        propBuilder.describe(PARENT_PROP, PARENT_PROP)
                .type(SnapType.COMPOSITE)
                .required()
                .withEntry(fileBrowsingChild)
                .withEntry(child)
                .add();

        // Table Property
        SnapProperty fileBrowsingColumn = propBuilder.describe(COLUMN_FILE_BROWSER_PROP,
                COLUMN_FILE_BROWSER_PROP)
                .required()
                .fileBrowsing()
                .build();
        SnapProperty column = propBuilder.describe(COLUMN_PROP, COLUMN_PROP)
                .required()
                .build();
        propBuilder.describe(TABLE_PROP, TABLE_PROP)
                .type(SnapType.TABLE)
                .withEntry(fileBrowsingColumn)
                .withEntry(column)
                .add();
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
        password = propertyValues.getAsExpression(PASSWORD_PROP).eval(null);
        fileBrowser = propertyValues.getAsExpression(FILE_BROWSER_PROP).eval(null);

        // also consider storing a reference to the PropertyValues in an instance variable to be
        // used when when evaluating expressions against each incoming document in process()
        composite = buildComposite(propertyValues);
        table = buildTable(propertyValues);
    }

    /*
    Demonstrates using getAsExpression and getExpressionPropertyFor to retrieve parent and child
    components of a composite property set
     */
    protected Map<String, Object> buildComposite(final PropertyValues propertyValues) {
        // a Map to hold the evaluated property values
        Map<String, Object> composite = new LinkedHashMap<>();

        // get the parent composite as an expression
        // the parent will resolve to a map of the child key-value pairs for each child
        Map<String, Object> parent = propertyValues.getAsExpression(PARENT_PROP).eval(null);

        // the getExpressionPropertyFor() method allows accessing the child expression variables
        String fileBrowsingChild =
                propertyValues.getExpressionPropertyFor(parent, CHILD_FILE_BROWSER_PROP).eval(null);
        String child = propertyValues.getExpressionPropertyFor(parent, CHILD_PROP).eval(null);

        composite.put(CHILD_FILE_BROWSER_PROP, fileBrowsingChild);
        composite.put(CHILD_PROP, child);

        return composite;
    }

    /*
    Table properties are evaluated similarly to Composites, except they return Lists of Maps. The
    list can be iterated through (representing each "row" of the table) with each key-value pair
    representing a table property to be evaluated.
     */
    protected List<Map<String, Object>> buildTable(final PropertyValues propertyValues) {
        // a List of Maps to represent the evaluated property values in a table
        List<Map<String, Object>> table = new ArrayList<>();

        // get the table property as an expression
        List<Map<String, Object>> tableProp = propertyValues.getAsExpression(TABLE_PROP).eval(null);

        // for each row of the table, get the child properties as an expression
        for (Map<String, Object> aRow : tableProp) {
            Map<String, Object> row = new LinkedHashMap<>();

            String childFileBrowser =
                    propertyValues.getExpressionPropertyFor(aRow, COLUMN_FILE_BROWSER_PROP)
                            .eval(null);
            String column = propertyValues.getExpressionPropertyFor(aRow, COLUMN_PROP).eval(null);

            row.put(COLUMN_FILE_BROWSER_PROP, childFileBrowser);
            row.put(COLUMN_PROP, column);

            table.add(row);
        }

        return table;
    }

    @Override
    public void process(Document document, String inputViewName) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(PASSWORD_PROP, getPassword());
        data.put(FILE_BROWSER_PROP, getFileBrowser());
        data.put("composite", getComposite());
        data.put("table", getTable());

        outputViews.write(documentUtility.newDocument(data), document);
    }

    public String getPassword() {
        return password;
    }

    public String getFileBrowser() {
        return fileBrowser;
    }

    public Map<String, Object> getComposite() {
        return composite;
    }

    public List<Map<String, Object>> getTable() {
        return table;
    }
}