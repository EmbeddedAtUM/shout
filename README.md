Shout for Android
=================

> Twitter for people that you don't know that you know.

Compiling
---------

Shout uses Apache Maven for dependency resolution, building, and testing. To 
link this project with Eclipse, you should have done the following:

1. Install Java 7 JDK, have an Android VM set up with the Android Dev Tools, 
install ADT plugin for Eclipse (obviously).

2. Install Apache Maven somewhere.

3. Set $MAVEN\_HOME to point to your Maven install directory, $ANDROID\_HOME to point to the the root Android SDK directory, $JAVA\_HOME to point to your JRE.

4. Add $MAVEN\_HOME/bin, $ANDROID\_HOME/tools, $ANDROID\_HOME\platform-tools to your system $PATH variable (via .bashrc, system configuration, etc.).

5. In the Shout directory with pom.xml, run `mvn eclipse:eclipse`. This will 
generate the .project and .classpath files needed by Eclipse to run. Then import
the project into your Eclipse workspace as a pre-existing Eclipse project.

More Eclipse Goodness
---------------------

If you're a fan of Eclipse plugins, install m2e-eclipse and the m2android 
plugin, then use the `pom.xml` to create a Maven Project and run from inside 
Eclipse.
