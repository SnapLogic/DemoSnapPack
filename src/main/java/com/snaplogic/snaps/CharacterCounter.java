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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.snaplogic.api.ConfigurationException;
import com.snaplogic.api.ExecutionException;
import com.snaplogic.common.properties.builders.PropertyBuilder;
import com.snaplogic.snap.api.BinaryOutput;
import com.snaplogic.snap.api.Document;
import com.snaplogic.snap.api.PropertyValues;
import com.snaplogic.snap.api.SnapDataException;
import com.snaplogic.snap.api.capabilities.Errors;
import com.snaplogic.snap.api.capabilities.General;
import com.snaplogic.snap.api.capabilities.Outputs;
import com.snaplogic.snap.api.capabilities.Version;
import com.snaplogic.snap.api.capabilities.ViewType;
import com.snaplogic.snap.api.write.SimpleBinaryWriteSnap;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A Snap that counts the number of occurrences of each letter in the English language for the
 * incoming data, and writes the result to a binary output view
 */
@General(title = "Character Counter", purpose = "Demo writing to Binary Output View",
        author = "Your Company Name", docLink = "http://yourdocslinkhere.com")
@Outputs(min = 1, max = 1, offers = ViewType.BINARY)
@Errors(min = 1, max = 1, offers = ViewType.DOCUMENT)
@Version(snap = 1)
public class CharacterCounter extends SimpleBinaryWriteSnap {

    @Override
    public void defineProperties(PropertyBuilder propertyBuilder) {
    }

    @Override
    public void configure(PropertyValues propertyValues) throws ConfigurationException {
    }

    @Override
    protected void process(final Document header, final ReadableByteChannel readChannel) {
        final StringBuilder sb = new StringBuilder();

        // Guava Multiset
        final Multiset<Character> bagOfChars = HashMultiset.create();

        try (InputStream inputStream = Channels.newInputStream(readChannel)) {
            Reader reader = new InputStreamReader(new BufferedInputStream(inputStream), UTF_8);

            // read in each character
            int characterRead;
            while ((characterRead = reader.read()) != -1) {
                // add the lowercase version of the character to the Multiset
                bagOfChars.add((char) Character.toLowerCase(characterRead));
            }
        } catch (IOException e) {
            errorViews.write(new SnapDataException(e, e.getMessage()), header);
        }

        try {
            // for each letter of English alphabet, write a line with the number of times
            // it appeared in the input data
            for (char letter = 'a'; letter <= 'z'; letter++) {
                sb.append(letter).append(":").append(bagOfChars.count(letter))
                        .append(System.lineSeparator());
            }
        } catch (Exception e) {
            // write to the error view when a problem processing the input data is encountered
            SnapDataException ex = new SnapDataException(e, "Unable to complete counting "
                    + "characters from input data.").withResolutionAsDefect();

            LinkedHashMap<String, String> data = new LinkedHashMap<>();
            data.put("content", sb.toString());

            errorViews.write(ex, documentUtility.newDocument(data));
            return;
        }

        outputViews.write(new BinaryOutput() {
            @Override
            public Document getHeader() {
                return header;
            }

            @Override
            public void write(WritableByteChannel writeChannel) throws IOException {
                OutputStream outputStream = Channels.newOutputStream(writeChannel);
                try {
                    IOUtils.write(sb.toString(), outputStream, UTF_8);
                } finally {
                    IOUtils.closeQuietly(outputStream);
                }
            }
        });
    }

    @Override
    public void cleanup() throws ExecutionException {
        // no-op
    }
}

