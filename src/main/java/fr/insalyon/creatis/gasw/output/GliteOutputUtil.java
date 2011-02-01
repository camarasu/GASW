/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.creatis.insa-lyon.fr/~silva
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.gasw.output;

import fr.insalyon.creatis.gasw.Constants;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.bean.Job;
import fr.insalyon.creatis.gasw.dao.DAOException;
import fr.insalyon.creatis.gasw.dao.DAOFactory;
import fr.insalyon.creatis.gasw.dao.JobDAO;
import fr.insalyon.creatis.gasw.monitor.Monitor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Rafael Silva
 */
public class GliteOutputUtil extends OutputUtil {

    private static final Logger log = Logger.getLogger(GliteOutputUtil.class);

    public GliteOutputUtil(int startTime) {
        super(startTime);
    }

    public GaswOutput getOutputs(String jobID) {
        try {
            File outputDir = new File("/tmp/jobOutput");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }

            JobDAO jobDAO = DAOFactory.getDAOFactory().getJobDAO();
            Job job = jobDAO.getJobByID(jobID);

            if (job.getStatus() != Monitor.Status.CANCELLED) {

                String exec = "glite-wms-job-output --noint " + jobID;
                Process process = Runtime.getRuntime().exec(exec);
                process.waitFor();

                BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outputPath = "";

                String s = null;
                while ((s = r.readLine()) != null) {
                    if (s.startsWith("/tmp/jobOutput")) {
                        outputPath = s.trim();
                    }
                }

                File stdOut = getStdFile(job, ".out", Constants.OUT_ROOT, outputPath);
                File stdErr = getStdFile(job, ".err", Constants.ERR_ROOT, outputPath);

                File outTempDir = new File(outputPath);
                outTempDir.delete();

                int exitCode = parseStdOut(job, stdOut);

                File appStdOut = saveFile(job, ".app.out", Constants.OUT_ROOT, getAppStdOut());
                File appStdErr = saveFile(job, ".app.err", Constants.ERR_ROOT, parseStdErr(stdErr));

                return new GaswOutput(jobID, exitCode, appStdOut, appStdErr, stdOut, stdErr);

            } else {
                File stdOut = saveFile(job, ".out", Constants.OUT_ROOT, "Job Cancelled");
                File stdErr = saveFile(job, ".err", Constants.ERR_ROOT, "Job Cancelled");

                GaswOutput output = new GaswOutput(jobID, stdOut, stdErr, stdOut, stdErr);

                return output;
            }

        } catch (DAOException ex) {
            logException(log, ex);
        } catch (InterruptedException ex) {
            logException(log, ex);
        } catch (IOException ex) {
            logException(log, ex);
        }
        return null;
    }

    /**
     *
     *
     * @param job Job object
     * @param extension File extension
     * @param outDir Output directory
     * @param srcDir Source directory
     * @return
     */
    private File getStdFile(Job job, String extension, String outDir, String srcDir) {
        File stdDir = new File(outDir);
        if (!stdDir.exists()) {
            stdDir.mkdir();
        }
        File stdFile = new File(srcDir + "/std" + extension);
        File stdRenamed = new File(outDir + "/" + job.getFileName() + ".sh" + extension);
        stdFile.renameTo(stdRenamed);
        return stdRenamed;
    }
}
