/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ajdt;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Configures AJDT project according to aspectj-maven-plugin configuration from pom.xml. Work in progress, most of
 * aspectj-maven-plugin configuration parameters is not supported yet.
 * 
 * @see http://mojo.codehaus.org/aspectj-maven-plugin/compile-mojo.html
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=160393
 * @author Igor Fedorenko
 * @author Eugene Kuleshov
 */
public class AjdtProjectConfigurator extends AbstractJavaProjectConfigurator {
  private static final Logger log = LoggerFactory.getLogger(AjdtProjectConfigurator.class);

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
    IProject project = request.getProject();

    configureNature(project, monitor);
  }

  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
      throws CoreException {
    IProject project = facade.getProject();
    // TODO cache in facade.setSessionProperty
    AspectJPluginConfiguration config = AspectJPluginConfiguration.create( //
        facade.getMavenProject(monitor), project);
    if(config != null) {
      for(IClasspathEntryDescriptor descriptor : classpath.getEntryDescriptors()) {
        String key = descriptor.getGroupId() + ":" + descriptor.getArtifactId();
        Set<String> aspectLibraries = config.getAspectLibraries(); // from pom.xml
        if(aspectLibraries != null && aspectLibraries.contains(key)) {
          //descriptor.addClasspathAttribute(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
          descriptor.getClasspathAttributes().put(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE_NAME,
              AspectJCorePreferences.ASPECTPATH_ATTRIBUTE_NAME);
          continue;
        }
        Set<String> inpathDependencies = config.getInpathDependencies();
        if(inpathDependencies != null && inpathDependencies.contains(key)) {
          //descriptor.addClasspathAttribute(AspectJCorePreferences.INPATH_ATTRIBUTE);
          descriptor.getClasspathAttributes().put(AspectJCorePreferences.INPATH_ATTRIBUTE_NAME,
              AspectJCorePreferences.INPATH_ATTRIBUTE_NAME);
        }
      }
    }
  }

  protected File[] getSourceFolders(ProjectConfigurationRequest request, MojoExecution mojoExecution)
      throws CoreException {

    // note: don't check for the aj nature here since this method may be called before the configure method.
    File[] sourceFolders = new File[0];
    File value = getParameterValue("aspectDirectory", File.class, request.getMavenSession(), mojoExecution);
    if(value != null) {
      IMavenProjectFacade facade = request.getMavenProjectFacade();
      IPath path = getFullPath(facade, value);
      if(value.exists()) {
        log.info("Found aspect source folder " + path);
        sourceFolders = new File[] {value};
      } else {
        log.warn("File " + path + " does not exist yet. Create it and re-run configuration.");
      }
    } else {
      log.info("No aspect source folder found. Failing back to 'src/main/aspect'");
      value = new File("src/main/aspect");
    }
    return sourceFolders;
  }

  static boolean isAjdtProject(IProject project) {
    try {
      return project != null && project.isAccessible() && project.hasNature(AspectJPlugin.ID_NATURE);
    } catch(CoreException e) {
      return false;
    }
  }

  private void configureNature(IProject project, IProgressMonitor monitor) throws CoreException {
    // Have to do this, since this may run before the jdt configurer
    if(!project.hasNature(JavaCore.NATURE_ID)) {
      addNature(project, JavaCore.NATURE_ID, monitor);
    }

    if(!project.hasNature(AspectJPlugin.ID_NATURE)) {
      addNature(project, AspectJPlugin.ID_NATURE, monitor);
    }
  }
}
