/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.doclet;

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.HtmlDoclet;


/**
 * A doclet which delegates the work to the standard doclet, except for the {@code -stylesheet} option.
 * Rather than overwriting the standard stylesheet with the given one, this keep both the standard and
 * the specified stylesheets as separated files. The standard stylesheet will be renamed {@code standard.css}.
 * The stylesheet provided by the user shall contains an import statement for the standard stylesheet.
 *
 * <p>This class presumes that all CSS files are encoded in UTF-8.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 */
public final class Doclet extends HtmlDoclet {
    /**
     * The encoding to use for reading and writing CSS files.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Invoked by Javadoc for starting the doclet.
     *
     * @param  root The root document.
     * @return {@code true} on success, or {@code false} on failure.
     */
    public static boolean start(final RootDoc root) {
        String stylesheetFile  = null;
        String outputDirectory = null;
        final String[][] options = root.options();
        final String[][] filteredOptions = new String[options.length][];
        int n = 0;
        for (final String[] option : options) {
            if (option.length == 2) {
                final String f = option[0];
                /*switch (option[0])*/ {
                    if (f.equals("-d")) {
                        outputDirectory = option[1];
                    }
                    else if (f.equals("-stylesheet") || f.equals("-stylesheetfile")) {
                        stylesheetFile = option[1];
                        continue; // Do not copy this option.
                    }
                }
            }
            filteredOptions[n++] = option;
        }
        final boolean status = HtmlDoclet.start(new FilteredRootDoc(root, Arrays.copyOf(filteredOptions, n)));
        if (stylesheetFile != null && outputDirectory != null) try {
            final File input  = new File(stylesheetFile);
            final File output = new File(outputDirectory);
            copyStylesheet(input, output);
            copyResources(input.getParentFile(), output);
        } catch (IOException e) {
            final StringWriter buffer = new StringWriter();
            final PrintWriter p = new PrintWriter(buffer);
            e.printStackTrace(p);
            root.printError(buffer.toString());
            return false;
        }
        return status;
    }

    /**
     * Opens a CSS file for reading.
     */
    private static BufferedReader openReader(final File file) throws IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), ENCODING));
    }

    /**
     * Opens a CSS file for writing.
     */
    private static BufferedWriter openWriter(final File file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));
    }

    /**
     * Copies the standard CSS file, then copies the custom CSS file.
     *
     * @param  inputFile        The standard CSS file generated by the standard doclet.
     * @param  outputDirectory  The directory where to copy the CSS file.
     * @throws IOException      If an error occurred while reading or writing.
     */
    private static void copyStylesheet(final File inputFile, final File outputDirectory) throws IOException {
        final File stylesheetFile = new File(outputDirectory, "stylesheet.css");
        final File standardFile   = new File(outputDirectory, "standard.css");
        /*
         * Copy the standard CSS file, skipping the import of DejaVu font
         * since our custom CSS file does not use it.
         */
        BufferedReader in  = openReader(stylesheetFile);
        BufferedWriter out = openWriter(standardFile);
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.equals("@import url('resources/fonts/dejavu.css');")) {
                    out.write(line);
                    out.newLine();
                }
            }
        } finally {
            out.close();
            in.close();
        }
        /*
         * Copy the custom CSS file, skipping comments for more compact file.
         */
        in  = openReader(inputFile);
        out = openWriter(stylesheetFile);
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() < 2 || line.charAt(1) != '*') {
                    out.write(line);
                    out.newLine();
                }
            }
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * Creates links to Javadoc resources in the top-level directory (not from "{@code doc-files}" subdirectories).
     * While the Maven documentation said that the "{@code src/main/javadoc}" directory is copied by default, or a
     * directory can be specified with {@code <javadocResourcesDirectory>}, I have been unable to make it work even
     * with absolute paths.
     *
     * @param  inputFile        The directory containing resources.
     * @param  outputDirectory  The directory where to copy the resource files.
     * @throws IOException      If an error occurred while reading or writing.
     */
    private static void copyResources(final File inputDirectory, final File outputDirectory) throws IOException {
        final File[] inputFiles = inputDirectory.listFiles(new FilenameFilter() {
            @Override public boolean accept(final File dir, final String name) {
                return !name.startsWith(".") &&
                       !name.equals("overview.html") &&
                       !name.equals("stylesheet.css");
            }
        });
        final byte[] buffer = new byte[4096];
        for (final File input : inputFiles) {
            final FileInputStream  in  = new FileInputStream(input);
            final FileOutputStream out = new FileOutputStream(new File(outputDirectory, input.getName()));
            try {
                int c;
                while ((c = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, c);
                }
            } finally {
                out.close();
                in.close();
            }
        }
    }
}
