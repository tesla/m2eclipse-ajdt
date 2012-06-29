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
import java.util.Arrays;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @author Igor Fedorenko
 */
@SuppressWarnings("restriction")
public class AjdtProjectConfiguratorTest extends AbstractMavenProjectTestCase {
  
//  private String origGoalsOnImport;
  
  private Display display;

  protected void setUp() throws Exception {
    super.setUp();
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      
      public void run() {
        JavaPlugin.getDefault();
      }
    });

//    origGoalsOnImport = mavenConfiguration.getGoalOnImport();
//    mavenConfiguration.setGoalOnImport("process-test-classes");
  }

  protected void tearDown() throws Exception {
//    mavenConfiguration.setGoalOnImport(origGoalsOnImport);
//
    if (display != null) {
      display.dispose();
    }
    super.tearDown();
  }
  
  public void testSimple01_import() throws IOException, CoreException, InterruptedException {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/p01/pom.xml", configuration);
    
    waitForJobsToComplete();
    
    MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, monitor);
    
    assertTrue("Expected AJDT nature", project.hasNature(AspectJPlugin.ID_NATURE));

    IJavaProject javaProject = JavaCore.create(project);
    List<IClasspathEntry> sources = getSources(javaProject.getRawClasspath());
    assertEquals(sources.toString(), 1, sources.size());
    assertEquals(project.getFolder("src/main/aspect").getFullPath(), sources.get(0).getPath());

    String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);
    assertEquals("The result of getResolvedProjectAspectPath is always an array of length 3", 3, aspectPath.length);
    assertTrue(aspectPath[0].contains("/maven-core-"));
    assertEquals("Should be one segment of this aspect path", aspectPath[0].length()-1, aspectPath[0].indexOf(':'));
    assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void testSimple02_import() throws IOException, CoreException, InterruptedException {
    ResolverConfiguration configuration = new ResolverConfiguration();
    IProject project = importProject("projects/p02/pom.xml", configuration);
    
    waitForJobsToComplete();
    
    MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(project, monitor);
    
    assertTrue("Expected AJDT nature", project.hasNature(AspectJPlugin.ID_NATURE));
    
    IJavaProject javaProject = JavaCore.create(project);
    List<IClasspathEntry> sources = getSources(javaProject.getRawClasspath());
    assertEquals(sources.toString(), 3, sources.size());
    assertEquals(project.getFolder("src/main/java").getFullPath(), sources.get(0).getPath());
    assertEquals(project.getFolder("src/test/java").getFullPath(), sources.get(1).getPath());
    assertEquals(project.getFolder("src/main/aspect").getFullPath(), sources.get(2).getPath());
    
    String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(project);
    assertEquals("The result of getResolvedProjectAspectPath is always an array of length 3", 3, aspectPath.length);
    assertTrue(aspectPath[0].contains("/maven-core-"));
    assertEquals("Should be one segment of this aspect path", aspectPath[0].length()-1, aspectPath[0].indexOf(':'));
    assertTrue("Content kind should be BINARY",aspectPath[1].startsWith("2")); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Entry kind should be LIBRARY", aspectPath[2].startsWith("1")); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  // TODOO disabled
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
    
    MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(projects[0], monitor);
    MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(projects[1], monitor);
    MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(projects[2], monitor);


    assertTrue("Expected AJ nature", projects[1].hasNature(AspectJPlugin.ID_NATURE));
    assertTrue("Expected AJ nature", projects[2].hasNature(AspectJPlugin.ID_NATURE));
    assertFalse("Should not have AJ nature", projects[3].hasNature(AspectJPlugin.ID_NATURE));
    
    String[] project1InPath = AspectJCorePreferences.getResolvedProjectInpath(projects[1]);
    String[] project1AspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(projects[1]);
    String[] project2InPath = AspectJCorePreferences.getResolvedProjectInpath(projects[2]);
    String[] project2AspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(projects[2]);
    String[] project3InPath = AspectJCorePreferences.getResolvedProjectInpath(projects[3]);
    String[] project3AspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(projects[3]);
    
    assertTrue("Invalid aspect path for project 'depa': " + project1InPath[0], project1InPath[0].contains("/depi/target/classes"));
    assertTrue("Invalid aspect path for project 'depa': " + project1InPath[0], project1InPath[0].contains("junit-3.8.jar"));
    assertEquals("Invalid aspect path for project 'aspect'", "", project1AspectPath[0]);
    
    assertEquals("Invalid in path for project 'depa'", "", project2InPath[0]);
    // note: this is actually a bug in ajdt that the aspect path contains target/classes twice this is because the aspect folder has two source folders whose output folders go to target/classes 
    assertTrue("Invalid aspect path for project 'depa': " + project2AspectPath[0], project2AspectPath[0].contains("/aspect/target/classes:"));
    assertTrue("Invalid aspect path for project 'depa': " + project2AspectPath[0], project2AspectPath[0].contains("junit-3.8.jar"));
    
    assertEquals("Invalid in path for project 'depi'", "", project3InPath[0]);
    assertEquals("Invalid aspect path for project 'depi'", "", project3AspectPath[0]);
  }
}
