/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ajdt;

import java.util.Set;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;


/**
 * Configures AJDT project according to aspectj-maven-plugin configuration from pom.xml. Work in progress, most of
 * aspectj-maven-plugin configuration parameters is not supported yet.
 * 
 * @see http://mojo.codehaus.org/aspectj-maven-plugin/compile-mojo.html
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=160393
 * @author Igor Fedorenko
 * @author Eugene Kuleshov
 */
public class AjdtProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
      
    // Have to do this, since this may run before the jdt configurer
    if(!project.hasNature(JavaCore.NATURE_ID)){
      addNature(project, JavaCore.NATURE_ID, monitor);
    }
    
    if(!project.hasNature(AspectJPlugin.ID_NATURE)) {
      addNature(project, AspectJPlugin.ID_NATURE, monitor);
    }
  }

  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
      throws CoreException {

    // TODO cache in facade.setSessionProperty
    AspectJPluginConfiguration config = AspectJPluginConfiguration.create( //
        facade.getMavenProject(monitor), facade.getProject());
    if(config != null) {
      for (IClasspathEntryDescriptor descriptor : classpath.getEntryDescriptors()) {
        String key = descriptor.getGroupId() + ":" + descriptor.getArtifactId();
        Set<String> aspectLibraries = config.getAspectLibraries(); // from pom.xml
        if(aspectLibraries != null && aspectLibraries.contains(key)) {
          descriptor.setClasspathAttribute(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE.getName(), AspectJCorePreferences.ASPECTPATH_ATTRIBUTE.getValue());
          continue;
        }
        Set<String> inpathDependencies = config.getInpathDependencies();
        if (inpathDependencies != null && inpathDependencies.contains(key)) {
          descriptor.setClasspathAttribute(AspectJCorePreferences.INPATH_ATTRIBUTE.getName(), AspectJCorePreferences.INPATH_ATTRIBUTE.getValue());
        }
      }
    }

  }

  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
    // TODO Auto-generated method configureRawClasspath
    
  }

}
