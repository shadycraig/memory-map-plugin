/*
 * The MIT License
 *
 * Copyright 2012 Praqma.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.praqma.jenkins.memorymap.parser;

import hudson.Extension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.jenkins.memorymap.graph.MemoryMapGraphConfiguration;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemory;
import net.praqma.jenkins.memorymap.result.MemoryMapConfigMemoryItem;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Praqma
 */
public class TexasInstrumentsMemoryMapParser extends AbstractMemoryMapParser {

    /*
     * Generic design
     */
    private static final Pattern MEMORY_OVERALL_SECTION = Pattern.compile("^\\.text\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    /*
     * Flash
     */
    private static final Pattern TEXT_DOT = Pattern.compile("^\\.text\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CONST_DOT = Pattern.compile("^\\.const\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern ECONST_DOT = Pattern.compile("^\\.econst\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern PINIT = Pattern.compile("^\\.pinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern SWITCH = Pattern.compile("^\\.switch\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    /*
     * Ram
     */
    private static final Pattern CINIT_DOT = Pattern.compile("^\\.cinit\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern STACK_DOT = Pattern.compile("^\\.stack\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern BSS_DOT = Pattern.compile("^\\.bss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern EBSS_DOT = Pattern.compile("^\\.ebss\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern SYSMEM = Pattern.compile("^\\.sysmem\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern ESYSMEM = Pattern.compile("^\\.esysmem\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern CIO = Pattern.compile("^\\.cio\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);
    private static final Pattern DATA = Pattern.compile("^\\.data\\s+\\S+\\s+\\S+\\s+(\\S+)", Pattern.MULTILINE);

    @DataBoundConstructor
    public TexasInstrumentsMemoryMapParser(String mapFile, String configurationFile, Integer wordSize, Boolean bytesOnGraph) {
        super(mapFile, configurationFile, wordSize, bytesOnGraph, TEXT_DOT, CONST_DOT, ECONST_DOT, PINIT, SWITCH, CINIT_DOT, STACK_DOT, BSS_DOT, EBSS_DOT, SYSMEM, ESYSMEM, CIO, DATA);
    }

    public TexasInstrumentsMemoryMapParser() {
        super();
    }

    @Override
    public MemoryMapConfigMemory parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) throws IOException {
        MemoryMapConfigMemory config = new MemoryMapConfigMemory();
        CharSequence sequence = createCharSequenceFromFile(f);
        for (MemoryMapGraphConfiguration graph : graphConfig) {
            String[] split = graph.getGraphDataList().split(",");
            for (String s : split) {
                s.trim();
                String[] multiSections = s.split("\\+");
                for (String ms : multiSections) {
                    Matcher m = MemoryMapConfigFileParserDelegate.getPatternForMemoryLayout(ms.replace(" ", "")).matcher(sequence);
                    MemoryMapConfigMemoryItem item = null;
                    while (m.find()) {
                        item = new MemoryMapConfigMemoryItem(m.group(1), m.group(3), m.group(5));
                        config.add(item);
                    }

                    if (item == null) {
                        logger.logp(Level.WARNING, "parseConfigFile", AbstractMemoryMapParser.class.getName(), String.format("parseConfigFile(List<MemoryMapGraphConfiguration> graphConfig, File f) non existing item: %s", s));
                        throw new IOException(String.format("No match found for program memory named %s", s));
                    }
                }

            }
        }
        return config;
    }

    @Override
    public MemoryMapConfigMemory parseMapFile(File f, MemoryMapConfigMemory config) throws IOException {
        return super.parseMapFile(f, config);
    }

    @Extension
    public static final class DescriptorImpl extends MemoryMapParserDescriptor<TexasInstrumentsMemoryMapParser> {

        @Override
        public String getDisplayName() {
            return "Texas Instruments";
        }

        @Override
        public AbstractMemoryMapParser newInstance(StaplerRequest req, JSONObject formData, AbstractMemoryMapParser instance) throws FormException {
            TexasInstrumentsMemoryMapParser parser = (TexasInstrumentsMemoryMapParser) instance;
            save();
            return parser;
        }
    }
}
