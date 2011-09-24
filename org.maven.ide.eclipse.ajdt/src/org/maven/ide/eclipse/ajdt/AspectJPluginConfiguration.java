/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ajdt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AspectJ plugin configuration accessor 
 * 
 * <pre>
 * &lt;plugin&gt;
     &lt;groupId&gt;org.codehaus.mojo&lt;/groupId&gt;
     &lt;artifactId&gt;aspectj-maven-plugin&lt;/artifactId&gt; 
     &lt;version&gt;1.0-beta-4-20080124.150437-6&lt;/version&gt;
     &lt;executions&gt;
       &lt;execution&gt; 
         &lt;phase&gt;process-classes&lt;/phase&gt;
         &lt;goals&gt;
           &lt;goal&gt;compile&lt;/goal&gt;
         &lt;/goals&gt;
       &lt;/execution&gt;
     &lt;/executions&gt;
     &lt;configuration&gt;
       &lt;source&gt;${maven.compile.source}&lt;/source&gt;
       &lt;target&gt;${maven.compile.target}&lt;/target&gt;
       &lt;complianceLevel&gt;${maven.compile.source}&lt;/complianceLevel&gt;
       &lt;includes&gt;
         &lt;include&gt;** / *.aj&lt;/include&gt;
       &lt;/includes&gt;
       &lt;bootclasspath&gt;${maven.compile.runtime}&lt;/bootclasspath&gt;
       &lt;encoding&gt;${project.build.sourceEncoding}&lt;/encoding&gt;
       &lt;verbose&gt;true&lt;/verbose&gt;
     &lt;/configuration&gt;
   &lt;/plugin&gt;
 * </pre>
 * 
 * @see http://mojo.codehaus.org/aspectj-maven-plugin/compile-mojo.html
 * @author Igor Fedorenko
 * @author Eugene Kuleshov
 */
class AspectJPluginConfiguration {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AspectJPluginConfiguration.class);
  
  Plugin plugin;

  AspectJPluginConfiguration(Plugin plugin) {
    this.plugin = plugin;
  }

  public Set<String> getAspectLibraries() {
    return getModules("aspectLibraries", "aspectLibrary");
  }
  
  public Set<String> getInpathDependencies() {
    return getModules("weaveDependencies", "weaveDependency");
  }

  private Set<String> getModules(String names, String name) {
    Set<String> result = new LinkedHashSet<String>();

    collectModules(result, ((Xpp3Dom) plugin.getConfiguration()), names, name);

    List<PluginExecution> executions = plugin.getExecutions();
    if (executions != null) {
      for (PluginExecution execution : executions) {
        collectModules(result, ((Xpp3Dom) execution.getConfiguration()), names, name);
      }
    }

    return result;
  }

  private String getElementValue(Xpp3Dom parent, String childName) {
    Xpp3Dom element = parent.getChild(childName);
    if(element == null ) {
      return null;
    }
    return element.getValue();
  }
  
  private void collectModules(Set<String> result, Xpp3Dom dom, String names, String name) {
    if (dom == null) {
      return;
    }

    Xpp3Dom aspectLibraries = dom.getChild(names);
    if (aspectLibraries == null) {
      return;
    }
    
    Xpp3Dom[] aspectLibrary = aspectLibraries.getChildren(name);
    if (aspectLibrary == null) {
      return;
    }

    for(int i = 0; i < aspectLibrary.length; i++ ) {
      String groupId = getElementValue(aspectLibrary[i], "groupId");
      if(groupId==null) {
        LOGGER.warn("groupId not found");
        continue;
      }
      String artifactId = getElementValue(aspectLibrary[i],"artifactId");
      if(artifactId==null) {
        LOGGER.warn("artifacId not found");
        continue;
      }
      result.add(groupId + ":" + artifactId);
    }
  }
  
  static boolean isAspectJProject(MavenProject mavenProject, IProject project) {
    Plugin plugin = getAspectJPlugin(mavenProject);

    if(plugin != null && plugin.getExecutions() != null && !plugin.getExecutions().isEmpty()) {
      return true;
    }

    return false;

    // projects without aspects can legitimately use aspects defined somewhere else
  }

  static AspectJPluginConfiguration create(MavenProject mavenProject, IProject project) {
    if(isAspectJProject(mavenProject, project)) {
      return new AspectJPluginConfiguration(getAspectJPlugin(mavenProject));
    }
    return null;
  }
  
  private static Plugin getAspectJPlugin(MavenProject mavenProject) {
    return mavenProject.getPlugin("org.codehaus.mojo:aspectj-maven-plugin");
  }

}
