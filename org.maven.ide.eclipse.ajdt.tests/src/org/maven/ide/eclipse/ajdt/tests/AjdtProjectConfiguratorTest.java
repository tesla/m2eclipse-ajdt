/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ajdt.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;

/**
 * @author Igor Fedorenko
 */
@SuppressWarnings("restriction")
public class AjdtProjectConfiguratorTest extends AbstractMavenProjectTestCase {
  
  public void testSimple01_import() throws IOException, CoreException, InterruptedException {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/p01/pom.xml", configuration);
    
    waitForJobsToComplete();
    
    assertTrue("Expected AJDT nature", project.hasNature(AspectJPlugin.ID_NATURE));

    IJavaProject javaProject = JavaCore.create(project);
    List<IClasspathEntry> sources = getSources(javaProject.getRawClasspath());
    assertEquals(1, sources.size());
    assertEquals(project.getFolder("src/main/aspect").getFullPath(), sources.get(0).getPath());

    String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);
    assertEquals("The result of getResolvedProjectAspectPath is always an array of length 3", 3, aspectPath.length);
    assertTrue(aspectPath[0].contains("/maven-core-"));
    assertEquals("Should be one segment of this aspect path", aspectPath[0].length()-1, aspectPath[0].indexOf(':'));
    assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void _test_interProject() throws IOException, CoreException {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject[] projects = importProjects("projects", new String[] {"/p01/pom.xml", "/p02/pom.xml"}, configuration);
    
    String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(projects[1]);
    assertEquals(1, aspectPath.length);
    assertTrue(aspectPath[0].endsWith("p01"));
  }

  private List<IClasspathEntry> getSources(IClasspathEntry[] rawCp) {
    ArrayList<IClasspathEntry> sources = new ArrayList<IClasspathEntry>();
    for (IClasspathEntry entry : rawCp) {
      if (IClasspathEntry.CPE_SOURCE == entry.getEntryKind()) {
        sources.add(entry);
      }
    }
    return sources;
  }
  
  public void testInterProject() throws Exception {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject[] projects = importProjects("projects/interproject", new String[] {"pom.xml", "aspect/pom.xml", "depa/pom.xml", "depi/pom.xml"}, configuration);
    
    // the real test would need to check if aspect/in paths are set properly
    // don't know yet if there will be common API for that in AJDT 1.5 and 1.6

    assertTrue("Expected AJDT nature", projects[1].hasNature(AspectJPlugin.ID_NATURE));
    assertTrue("Expected AJDT nature", projects[2].hasNature(AspectJPlugin.ID_NATURE));
  }
}
