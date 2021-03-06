package com.philemonworks.selfdiagnose.report;

import com.philemonworks.selfdiagnose.*;
import com.philemonworks.selfdiagnose.check.CheckProperty;
import com.philemonworks.selfdiagnose.check.CheckResourceProperty;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ReportMavenPOMProperties is a task that reports the Maven build properties
 * which are included in the application if it is build using Maven.
 * Usage:
 * <pre>
 * &lt;reportmavenpomproperties
 *  comment="Maven POM properties"
 *  name="/META-INF/maven/com.yours.appp/myapp/pom.properties"
 * /&gt;
 * </pre>
 * @author ernestmicklei
 */
public class ReportMavenPOMProperties extends CheckResourceProperty {
    private static final long serialVersionUID = -1621931990560851769L;

    public ReportMavenPOMProperties() {
        setSeverity(Severity.NONE);
    }

    public String getDescription() {
        return "Reports on the pom.properties generated by Maven";
    }

    public void setUp(ExecutionContext ctx) throws DiagnoseException {
        if (name == null)
            DiagnoseUtil.verifyNonEmptyString(PARAMETER_URL, url, CheckProperty.class);
    }

    public void run(ExecutionContext ctx, DiagnosticTaskResult result) throws DiagnoseException {
        InputStream is = null;
        String version = null;
        String buildtime = null;
        try {
          Object servletContext = ctx.getValue("servletcontext");
          if (servletContext != null && servletContext instanceof ServletContext) {
             is = ((ServletContext) servletContext).getResourceAsStream(this.getName());
          }
          if (is == null) {
              is = this.getClass().getResourceAsStream(this.getName());
          }
          // if needed retry using system resource
          if (is == null) {
              is = ClassLoader.getSystemResourceAsStream(this.getName());
          }
          // if needed retry using contextClassLoader
          if (is == null) {
              is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.getName());
          }
          if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int linenumber = 0;
            while ((line = br.readLine()) != null) {
              linenumber++;
              if (linenumber == 2 && line.startsWith("#")) {
                buildtime = line.substring(1);
              }
              if (line.startsWith("version=")) {
                version = line.substring(8);
              }
            }
          } else {
             result.setFailedMessage("Could not find version properties file (tried all classloaders i know of): [" + this.getName() + "]");
             return;
          }
        } catch (final IOException e) {
            result.setErrorMessage("Could not load version properties file");
            return;
        } finally {
          if (is != null) {
            try {
              is.close();
            } catch (final IOException e) {
                result.setErrorMessage("Could not close input stream");
                return;
            }
          }
        }

        if (version == null || version.length() == 0) {
          version = "unknown";
        }
        if (buildtime == null || version.length() == 0) {
          buildtime = "an unknown time";
        }
        result.setPassedMessage("Version=" + version + " build=" +  buildtime + " from [" + getName() + "]");
    }

}
