/*
 * Copyright 2009 David Jurgens
 *
 * This file is part of the S-Space package and is covered under the terms and
 * conditions therein.
 *
 * The S-Space package is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation and distributed hereunder to you.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND NO REPRESENTATIONS OR WARRANTIES,
 * EXPRESS OR IMPLIED ARE MADE.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, WE MAKE
 * NO REPRESENTATIONS OR WARRANTIES OF MERCHANT- ABILITY OR FITNESS FOR ANY
 * PARTICULAR PURPOSE OR THAT THE USE OF THE LICENSED SOFTWARE OR DOCUMENTATION
 * WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER
 * RIGHTS.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.IOError;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * A collection of static methods for processing text.
 *
 * @author David Jurgens
 */
public class StringUtils {

    /**
     * Uninstantiable
     */ 
    private StringUtils() {}
    
    /**
     * A mapping from HTML codes for escaped special characters to their unicode
     * character equivalents.
     */
    private static final Map<String,String> HTML_CODES
	= new HashMap<String,String>();

    private static final Map<String,String> LATIN1_CODES
	= new HashMap<String,String>();


    static {
	HTML_CODES.put("&nbsp;"," ");
	HTML_CODES.put("&Agrave;","�");
	HTML_CODES.put("&Aacute;","�");
	HTML_CODES.put("&Acirc;","�");
	HTML_CODES.put("&Atilde;","�");
	HTML_CODES.put("&Auml;","�");
	HTML_CODES.put("&Aring;","�");
	HTML_CODES.put("&AElig;","�");
	HTML_CODES.put("&Ccedil;","�");
	HTML_CODES.put("&Egrave;","�");
	HTML_CODES.put("&Eacute;","�");
	HTML_CODES.put("&Ecirc;","�");
	HTML_CODES.put("&Euml;","�");
	HTML_CODES.put("&Igrave;","�");
	HTML_CODES.put("&Iacute;","�");
	HTML_CODES.put("&Icirc;","�");
	HTML_CODES.put("&Iuml;","�");
	HTML_CODES.put("&ETH;","�");
	HTML_CODES.put("&Ntilde;","�");
	HTML_CODES.put("&Ograve;","�");
	HTML_CODES.put("&Oacute;","�");
	HTML_CODES.put("&Ocirc;","�");
	HTML_CODES.put("&Otilde;","�");
	HTML_CODES.put("&Ouml;","�");
	HTML_CODES.put("&Oslash;","�");
	HTML_CODES.put("&Ugrave;","�");
	HTML_CODES.put("&Uacute;","�");
	HTML_CODES.put("&Ucirc;","�");
	HTML_CODES.put("&Uuml;","�");
	HTML_CODES.put("&Yacute;","�");
	HTML_CODES.put("&THORN;","�");
	HTML_CODES.put("&szlig;","�");
	HTML_CODES.put("&agrave;","�");
	HTML_CODES.put("&aacute;","�");
	HTML_CODES.put("&acirc;","�");
	HTML_CODES.put("&atilde;","�");
	HTML_CODES.put("&auml;","�");
	HTML_CODES.put("&aring;","�");
	HTML_CODES.put("&aelig;","�");
	HTML_CODES.put("&ccedil;","�");
	HTML_CODES.put("&egrave;","�");
	HTML_CODES.put("&eacute;","�");
	HTML_CODES.put("&ecirc;","�");
	HTML_CODES.put("&euml;","�");
	HTML_CODES.put("&igrave;","�");
	HTML_CODES.put("&iacute;","�");
	HTML_CODES.put("&icirc;","�");
	HTML_CODES.put("&iuml;","�");
	HTML_CODES.put("&eth;","�");
	HTML_CODES.put("&ntilde;","�");
	HTML_CODES.put("&ograve;","�");
	HTML_CODES.put("&oacute;","�");
	HTML_CODES.put("&ocirc;","�");
	HTML_CODES.put("&otilde;","�");
	HTML_CODES.put("&ouml;","�");
	HTML_CODES.put("&oslash;","�");
	HTML_CODES.put("&ugrave;","�");
	HTML_CODES.put("&uacute;","�");
	HTML_CODES.put("&ucirc;","�");
	HTML_CODES.put("&uuml;","�");
	HTML_CODES.put("&yacute;","�");
	HTML_CODES.put("&thorn;","�");
	HTML_CODES.put("&yuml;","�");
	HTML_CODES.put("&lt;","<");
	HTML_CODES.put("&gt;",">");
	HTML_CODES.put("&quot;","\"");
	HTML_CODES.put("&amp;","&");        

	LATIN1_CODES.put("&#039;", "'");
	LATIN1_CODES.put("&#160;", " ");
	LATIN1_CODES.put("&#162;", "�");
	LATIN1_CODES.put("&#164;", "�");
	LATIN1_CODES.put("&#166;", "�");
	LATIN1_CODES.put("&#168;", "�");
	LATIN1_CODES.put("&#170;", "�");
	LATIN1_CODES.put("&#172;", "�");
	LATIN1_CODES.put("&#174;", "�");
	LATIN1_CODES.put("&#176;", "�");
	LATIN1_CODES.put("&#178;", "�");
	LATIN1_CODES.put("&#180;", "�");
	LATIN1_CODES.put("&#182;", "�");
	LATIN1_CODES.put("&#184;", "�");
	LATIN1_CODES.put("&#186;", "�");
	LATIN1_CODES.put("&#188;", "�");
	LATIN1_CODES.put("&#190;", "�");
	LATIN1_CODES.put("&#192;", "�");
	LATIN1_CODES.put("&#194;", "�");
	LATIN1_CODES.put("&#196;", "�");
	LATIN1_CODES.put("&#198;", "�");
	LATIN1_CODES.put("&#200;", "�");
	LATIN1_CODES.put("&#202;", "�");
	LATIN1_CODES.put("&#204;", "�");
	LATIN1_CODES.put("&#206;", "�");
	LATIN1_CODES.put("&#208;", "�");
	LATIN1_CODES.put("&#210;", "�");
	LATIN1_CODES.put("&#212;", "�");
	LATIN1_CODES.put("&#214;", "�");
	LATIN1_CODES.put("&#216;", "�");
	LATIN1_CODES.put("&#218;", "�");
	LATIN1_CODES.put("&#220;", "�");
	LATIN1_CODES.put("&#222;", "�");
	LATIN1_CODES.put("&#224;", "�");
	LATIN1_CODES.put("&#226;", "�");
	LATIN1_CODES.put("&#228;", "�");
	LATIN1_CODES.put("&#230;", "�");
	LATIN1_CODES.put("&#232;", "�");
	LATIN1_CODES.put("&#234;", "�");
	LATIN1_CODES.put("&#236;", "�");
	LATIN1_CODES.put("&#238;", "�");
	LATIN1_CODES.put("&#240;", "�");
	LATIN1_CODES.put("&#242;", "�");
	LATIN1_CODES.put("&#244;", "�");
	LATIN1_CODES.put("&#246;", "�");
	LATIN1_CODES.put("&#248;", "�");
	LATIN1_CODES.put("&#250;", "�");
	LATIN1_CODES.put("&#252;", "�");
	LATIN1_CODES.put("&#254;", "�");
	LATIN1_CODES.put("&#34;", "\"");
	LATIN1_CODES.put("&#38;", "&");
	LATIN1_CODES.put("&#8217;", "'");
    }

    /**
     * Loads each line of the file as a list of strings.
     *
     * @throws IOError if any exception occurs while reading the file
     */
    public static List<String> loadFileAsList(File f) {
        try {
            List<String> s = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new FileReader(f));
            for (String line = null; (line = br.readLine()) != null; )
                s.add(line);
            br.close();
            return s;
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }
    }

    /**
     * Loads the contents of a file as a set of strings, with each line being
     * treated as a separate instance.
     *
     * @throws IOError if any exception occurs while reading the file
     */
    public static Set<String> loadFileAsSet(File f) {
        try {
            Set<String> s = new HashSet<String>();
            BufferedReader br = new BufferedReader(new FileReader(f));
            for (String line = null; (line = br.readLine()) != null; )
                s.add(line);
            br.close();
            return s;
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }
    }
    
    /**
     * Returns the provided string where all HTML special characters
     * (e.g. <pre>&nbsp;</pre>) have been replaced with their utf8 equivalents.
     *
     * @param source a String possibly containing escaped HTML characters
     */
    public static final String unescapeHTML(String source) {

	StringBuilder sb = new StringBuilder(source.length());

	// position markers for the & and ;
	int start = -1, end = -1;
	
	// the end position of the last escaped HTML character
	int last = 0;

	start = source.indexOf("&");
	end = source.indexOf(";", start);
	
	while (start > -1 && end > start) {
	    String encoded = source.substring(start, end + 1);
	    String decoded = HTML_CODES.get(encoded);

	    // if encoded form wasn't in the HTML codes, try checking to see if
	    // it was a Latin-1 code
	    if (decoded == null) {
		decoded = LATIN1_CODES.get(encoded);
	    }

	    if (decoded != null) {
		// append the string containing all characters from the last escaped
		// character to the current one
		String s = source.substring(last, start);
		sb.append(s).append(decoded);
		last = end + 1;
	    }
	    
	    start = source.indexOf("&", end);
	    end = source.indexOf(";", start);
	}
	// if there weren't any substitutions, don't both to create a new String
	if (sb.length() == 0)
	    return source;

	// otherwise finish the substitution by appending all the text from the
	// last substitution until the end of the string
	sb.append(source.substring(last));
	return sb.toString();
    }

    /**
     * Modifies the provided {@link StringBuilder} by replacing all HTML special
     * characters (e.g. <pre>&nbsp;</pre>) with their utf8 equivalents.
     *
     * @param source a String possibly containing escaped HTML characters
     */
    public static final void unescapeHTML(StringBuilder source) {

	// position markers for the & and ;
	int start = -1, end = -1;
	
	// the end position of the last escaped HTML character
	int last = 0;

	start = source.indexOf("&");
	end = source.indexOf(";", start);
	
	while (start > -1 && end > start) {
	    String encoded = source.substring(start, end + 1);
	    String decoded = HTML_CODES.get(encoded);

	    // if encoded form wasn't in the HTML codes, try checking to see if
	    // it was a Latin-1 code
	    if (decoded == null) {
		decoded = LATIN1_CODES.get(encoded);
	    }
            
            // If the string had encoded HTML that was recognized, replace it
            // with the decoded version
	    if (decoded != null) {
                source.replace(start, end + 1, decoded);
	    }
	    
            // Use the start+1 rather than end, since the decoded text may be
            // smaller than the encoded version.  However, don't use start in
            // case the decoded character was actually a '&'.
	    start = source.indexOf("&", start + 1);
	    end = source.indexOf(";", start);
	}
    }    
}
