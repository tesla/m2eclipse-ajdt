# m2e-ajdt-configurer

m2e-ajdt-configurer - eclipse plugin that helps you configure eclipse aspectj projects if you use m2e.

# Where to get

Thanks to Andrew there is an update site at: http://dist.springsource.org/release/AJDT/configurator

If that one doesn't work for you, you can always build your own.

# How to build 

## create local mirror of p2 repos

You should either download compressed p2 repositories (see ajdt sample below), or mirror existing p2 repositories (see [p2 cheatsheet](http://pweclipse.blogspot.com/2011/06/p2-cheatsheet.html), or http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring.

You will need:

* eclipse ide sdk - you can find list of eclipse update sites at (http://wiki.eclipse.org/Eclipse_Project_Update_Sites)
* swtbot (http://eclipse.org/swtbot/downloads.php)
* ajdt (http://www.eclipse.org/ajdt/downloads/)
* m2e (http://eclipse.org/m2e/download/)

Creating p2 mirror can take a long time, especially for eclipse sdk.
OTOH you can create p2 repo from an existing installation (see https://docs.sonatype.org/display/TYCHO/How+to+make+existing+OSGi+bundles+consumable+by+Tycho ):

 * download eclipse archive
 * unpack archive, assemble jars
 * execute p2 publisher in order to create local p2 repo

Here's the copy of Tycho wiki page:

```
Prerequisites:

Local Eclipse Galileo installation in %ECLIPSE_HOME%
We want to generate P2 metadata for a bunch of OSGi bundles which we have locally in the filesystem. Eclipse provides the FeaturesAndBundlesPublisher command line application for this task.

First of all we copy all bundle jars into a <BUNDLE_ROOT>/plugins directory
Then we execute
%ECLIPSE_HOME%\eclipsec.exe -debug -consolelog -nosplash -verbose -application
  org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher
  -metadataRepository file:/<BUNDLE_ROOT>/repo
  -artifactRepository file:<BUNDLE_ROOT>/repo
  -source <BUNDLE_ROOT> -compress -publishArtifacts
The result is a P2 repository with all OSGi bundles under <BUNDLE_ROOT>/repo. Note the generated P2 metadata files artifacts.jar and content.jar in the repo directory.
```

Optionally (if you want to share it with other people):

```
The P2 repository in <BUNDLE_ROOT>/repo is complete, we just need to make it available via HTTP so it can be globally referenced.

This could be done using any HTTP server such as Apache. In our case we chose to deploy it on Tomcat as we already have a tomcat running for other purposes such as Hudson etc.

On the host running tomcat, copy the contents of <BUNDLE_ROOT>/repo to <TOMCAT_HOME>/webapps/<YOUR_REPO_DIR>

From now on you could reference this P2 repository in pom.xml as

<repository>
  <id>tomcat-p2</id>
  <layout>p2</layout>
  <url>http://<TOMCAT_HOST>:<TOMCAT_PORT>/<YOUR_REPO_DIR></url>
</repository>

```

##  setup maven  settings.xml

### add p2 mirrors to settings.xml (make sure you enter actual p2 repo locations): 
```xml
	<mirrors>
		<mirror>
			<id>helios-local</id>
			<mirrorOf>helios,platform-e36</mirrorOf>
			<url>file://opt/java/eclipse/repos/3.6.2/helios</url>
			<layout>p2</layout>
			<mirrorOfLayouts>p2</mirrorOfLayouts>
		</mirror>
		<mirror>
			<id>indigo-local</id>
			<mirrorOf>indigo,platform-e37</mirrorOf>
			<url>file://opt/java/eclipse/repos/3.7/indigo</url>
			<layout>p2</layout>
			<mirrorOfLayouts>p2</mirrorOfLayouts>
		</mirror>
		<mirror>
			<id>ajdt-e36-local</id>
			<mirrorOf>ajdt-e36</mirrorOf>
			<url>jar:file:///opt/java/eclipse/repos/3.6.2/ajdt_2.1.3_for_eclipse_3.6.zip!/</url>
			<layout>p2</layout>
			<mirrorOfLayouts>p2</mirrorOfLayouts>
		</mirror>
		<mirror>
			<id>ajdt-e37-local</id>
			<mirrorOf>ajdt-e37</mirrorOf>
			<url>jar:file:///opt/java/eclipse/repos/3.7/ajdt_2.1.3_for_eclipse_3.7.zip!/</url>
			<layout>p2</layout>
			<mirrorOfLayouts>p2</mirrorOfLayouts>
		</mirror>
	</mirrors>

```

###  setup tycho specific profiles in settings.xml

(make sure the profiles are activated)

```xml
	<profiles>
		<profile>
			<id>tycho-helios</id>
			<repositories>
				<repository>
					<id>platform-e36</id>
					<url>http://download.eclipse.org/eclipse/updates/3.6</url>
					<name>Helios updates repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>helios</id>
					<url>http://download.eclipse.org/releases/helios</url>
					<name>Helios repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>swt-bot-remote</id>
					<url>http://download.eclipse.org/technology/swtbot/helios/dev-build/update-site/</url>
					<name>swt-bot repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>ajdt-e36</id>
					<url>http://download.eclipse.org/tools/ajdt/36/update</url>
					<name>ajdt repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>m2e</id>
<!--					<url>http://download.eclipse.org/technology/m2e/milestones/1.0</url> -->
					<url>http://download.eclipse.org/technology/m2e/releases/</url>
					<layout>p2</layout>
				</repository>
			</repositories>
		</profile>
		<profile>
			<id>tycho-indigo</id>
			<repositories>
				<repository>
					<id>platform-e37</id>
					<url>http://download.eclipse.org/eclipse/updates/3.7</url>
					<name>Indigo updates repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>indigo</id>
					<url>http://download.eclipse.org/releases/indigo</url>
					<name>Indigo repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>swt-bot-remote</id>
					<url>http://download.eclipse.org/technology/swtbot/helios/dev-build/update-site/</url>
					<name>swt-bot repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>ajdt-e37</id>
					<url>http://download.eclipse.org/tools/ajdt/37/update</url>
					<name>ajdt repo</name>
					<layout>p2</layout>
				</repository>
				<repository>
					<id>m2e</id>
<!--					<url>http://download.eclipse.org/technology/m2e/milestones/1.0</url> -->
					<url>http://download.eclipse.org/technology/m2e/releases/</url>
					<layout>p2</layout>
				</repository>
			</repositories>
		</profile>
	</profiles>

	<activeProfiles>
		<activeProfile>default</activeProfile>
		<activeProfile>tycho-indigo</activeProfile>
		<activeProfile>tycho-helios</activeProfile>
	</activeProfiles>
</settings>

```


### execute build

`mvn clean package`


## versioning

to set new version execute:
`mvn tycho-versions:set-version -DnewVersion=0.13.0.qualifier`

where qualifier is either -SNAPSHOT or [osgi qualifier](http://www.osgi.org/javadoc/r4v43/org/osgi/framework/Version.html):

```
Version identifiers have four components.

Major version. A non-negative integer.
Minor version. A non-negative integer.
Micro version. A non-negative integer.
Qualifier. A text string.

Created a version identifier from the specified string.
Here is the grammar for version strings.

 version ::= major('.'minor('.'micro('.'qualifier)?)?)?
 major ::= digit+
 minor ::= digit+
 micro ::= digit+
 qualifier ::= (alpha|digit|'_'|'-')+
 digit ::= [0..9]
 alpha ::= [a..zA..Z]
 
There must be no whitespace in version.

```

