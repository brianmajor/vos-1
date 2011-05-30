/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 4 $
*
************************************************************************
*/

package ca.nrc.cadc.util;

import java.io.Writer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.varia.LevelRangeFilter;

/**
 * Initialize log4j for the specified package and level. Logging is only
 * to the console.
 *
 */
public class Log4jInit
{
    // true after calling initialize
    private static boolean initialized = false;
    
    private static boolean consoleAppendersCreated = false;

    // SHORT_FORMAT applies to DEBUG and INFO logging levels
    private static final String SHORT_FORMAT = "%-4r [%t] %-5p %c{1} %x - %m\n";

    // LONG_FORMAT applies to WARN, ERROR and FATAL logging levels
    private static final String LONG_FORMAT = "%d{ISO8601} [%t] %-5p %c{1} %x - %m\n";

    // Appender for DEBUG and INFO with SHORT_FORMAT message prefix
    private static ConsoleAppender conAppenderLow = 
            new ConsoleAppender(new PatternLayout(SHORT_FORMAT));

    // Appender for WARN, ERROR and FATAL with LONG_FORMAT message prefix
    private static ConsoleAppender conAppenderHigh =
        new ConsoleAppender(new PatternLayout(LONG_FORMAT));

    static {
        // set the overall logging level to ERROR
        Logger.getRootLogger().setLevel(Level.ERROR);
    }
    /**
     * Initializes logging to the console.
     * 
     * @param pkg the name of package or ancestors of package or classes. Can't be null.
     * @param level the logging level.
     */
    public static synchronized void setLevel(String pkg, Level level)
    {
        if (!initialized)
            createLog4jConsoleAppenders();

        // set specified package and level
        Logger.getLogger(pkg).setLevel(level);
        initialized = true;
    }

    /**
     * Configure log4j for use in a GUI. All output is done using the
     * same format. This method sets up logging to the specified writer
     * on every call, so should be called with a non-null writer only once
     * (per writer). It may be called multiple times to set logging levels for
     * different packages.
     * 
     * @param pkg package to configure logging for
     * @param level log level for pkg
     * @param dest destination writer to log to  (may be null)
     */
    public static synchronized void setLevel(String pkg, Level level, Writer dest)
    {
        createLog4jWriterAppender(dest);

        // set specified package and level
        Logger.getLogger(pkg).setLevel(level);
    }

    /**
     * Clear all existing appenders, then create default console appenders for Log4j.
     * Any other extra file appender has to be initialized AFTER this method is called,
     * otherwise they would be cleared.
     * 
     */
    public static synchronized void createLog4jConsoleAppenders()
    {
        if (!consoleAppendersCreated)
        {
            BasicConfigurator.resetConfiguration();
            
            LevelRangeFilter errorFilter = new LevelRangeFilter();
            errorFilter.setLevelMax(Level.FATAL);
            errorFilter.setLevelMin(Level.WARN);
            errorFilter.setAcceptOnMatch(true);
            conAppenderHigh.clearFilters();
            conAppenderHigh.addFilter(errorFilter);
            BasicConfigurator.configure(conAppenderHigh);
    
            LevelRangeFilter infoFilter = new LevelRangeFilter();
            infoFilter.setLevelMax(Level.INFO);
            infoFilter.setLevelMin(Level.DEBUG);
            infoFilter.setAcceptOnMatch(true);
            conAppenderLow.clearFilters();
            conAppenderLow.addFilter(infoFilter);
            BasicConfigurator.configure(conAppenderLow);
            
            consoleAppendersCreated = true;
        }
    }
    
    /**
     * Create a Log4j appender which writes logs into a writer, i.e. a FileWriter.
     * 
     * @param writer
     */
    public static void createLog4jWriterAppender(Writer writer)
    {
        if (writer != null)
        {
            WriterAppender app = new WriterAppender(new PatternLayout(LONG_FORMAT), writer);
            LevelRangeFilter filter = new LevelRangeFilter();
            filter.setLevelMax(Level.FATAL);
            filter.setLevelMin(Level.DEBUG);
            filter.setAcceptOnMatch(true);
            app.clearFilters();
            app.addFilter(filter);
            BasicConfigurator.configure(app);
        }
    }
    
    /**
     * Set the layout format of console log for levels of WARN, ERROR, and FATAL.
     * 
     * @param formatString refer to org.apache.log4j.PatternLayout for usage. 
     */
    public static synchronized void setConsoleErrorLogLayoutFormat(String formatString)
    {
        conAppenderHigh.setLayout(new PatternLayout(formatString));
    }

    /**
     * Set the layout format of console log for levels of DEBUG and INFO.
     * 
     * @param formatString refer to org.apache.log4j.PatternLayout for usage. 
     */
    public static synchronized void setConsoleInfoLogLayoutFormat(String formatString)
    {
        conAppenderLow.setLayout(new PatternLayout(formatString));
    }

}
