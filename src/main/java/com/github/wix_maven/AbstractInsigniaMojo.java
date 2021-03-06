package com.github.wix_maven;

/*
 * #%L
 * WiX Toolset (Windows Installer XML) Maven Plugin
 * %%
 * Copyright (C) 2013 - 2014 GregDomjan NetIQ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Abstract Goal which executes WiX Insignia for<br> 
 * <li>inscribing an MSI with the digital signatures that its external CABs are signed with.
 * <li>detach/reattach burn engine from bundle
 */
public abstract class AbstractInsigniaMojo extends AbstractPackageable {

	/**
	 * Indicate if the bundleEngine should be detached for signing, and re attached.
	 */
	@Parameter(property = "wix.signBundleEngine", defaultValue = "false")
	protected boolean signBundleEngine;

	/**
	 * TODO: make this a pattern so no need for seperate filename
	 * The file path for detach/attach of the bundle engine. 
	 * - $(arch) ${culture}
	 */
	@Parameter(property = "wix.bundleEnginePath", defaultValue = "${project.build.directory}/wix-bundleEngines", required=true)
	protected File bundleEnginePath;

	/**
	 * TODO: merge with above once it is a pattern so no need for seperate filename
	 * The file name for detach/attach of the bundle engine.
	 */
	@Parameter(property = "wix.bundleEngineName", defaultValue = "engine.exe", required=true)
	protected String bundleEngineName;

	/**
	 * Prepare basic insignia command line
	 *  
	 * @param insigniaTool
	 * @throws MojoExecutionException
	 */
	protected Commandline insignia(File insigniaTool) throws MojoExecutionException {
		Commandline cl = new Commandline();

		cl.setExecutable(insigniaTool.getAbsolutePath());
		addToolsetGeneralOptions(cl);
		
		return cl;
	}

//	 usage: insignia.exe [-?] [-nologo] [-o[ut] outputPath] [@responseFile]
//	           [-im databaseFile|-ib bundleFile|-ab bundlePath bundleWithAttachedContainerPath]
//	   -im        specify the database file to inscribe
//	   -ib        specify the bundle file from which to extract the engine.
//	              The engine is stored in the file specified by -o
//	   -ab        Reattach the engine to a bundle.
//	   -nologo    skip printing insignia logo information
//	   -o[ut]     specify output file. Defaults to databaseFile or bundleFile.
//	              If out is a directory name ending in '\', output to a file with
//	               the same name as the databaseFile or bundleFile in that directory
//	    -sw[N]     suppress all warnings or a specific message ID
//	               (example: -sw1009 -sw1103)
//	    -swall     suppress all warnings (deprecated)
//	    -v         verbose output
//	    -wx[N]     treat all warnings or a specific message ID as an error
//	               (example: -wx1009 -wx1103)
//	   -wxall     treat all warnings as errors (deprecated)
//	   -? | -help this help information

	/**
	 * Execute the given command line parsing output for torch comments
	 * @param cl
	 * @throws MojoExecutionException
	 */
	protected void insignia(Commandline cl) throws MojoExecutionException {
		try {
			if (verbose) {
				getLog().info(cl.toString());
			} else {
				getLog().debug(cl.toString());
			}

			// TODO: maybe should report or do something with return value.
			int returnValue = CommandLineUtils.executeCommandLine(cl, new StreamConsumer() {

				public void consumeLine(final String line) {
					// TODO: torch specific message handling
					if (line.contains(" : error ")) {
						getLog().error(line);
					} else if (line.contains(" : warning ")) {
						getLog().warn(line);
					} else if (verbose) {
						getLog().info(line);
					} else {
						getLog().debug(line);
					}
				}

			}, new StreamConsumer() {

				public void consumeLine(final String line) {
					getLog().error(line);
				}

			});

			if (returnValue != 0) {
				throw new MojoExecutionException("Problem executing insignia, return code " + returnValue);
			}
		} catch (CommandLineException e) {
			// throw new MojoExecutionException( "Error running mapping-tools.",
			// e );
			throw new MojoExecutionException("Problem executing insignia", e);
		}
	}

	protected File validateTool() throws MojoExecutionException {
		File torchTool = new File(toolDirectory, "/bin/insignia.exe");
		if (!torchTool.exists())
			throw new MojoExecutionException("Insignia tool doesn't exist " + torchTool.getAbsolutePath());
		return torchTool;
	}
}

