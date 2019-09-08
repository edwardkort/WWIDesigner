Subsequent to java 8 (with the last released version being 1.8.0_221), there
have been many changes to the java language and its distribution. Java 8 is the
last version that is supported by Oracle free for personal use. From Oracle,
later versions are targeted for commercial use and for developers. However, 
later versions are available through the OpenJDK initiative (and the Eclipse 
Java initiative). 

WIDesigner will continue to be developed under java 8. There is nothing in
WIDesigner that is dependent upon later versions of java. But we will 
endeavor to ensure that WIDesigner is compatible with the later java versions.

The challenge is that WIDesigner uses third-party libraries (over which we
have no control) which may not be fully compatible with later java versions.
There are two such sets of libraries that we had to address in the first
"compatible" release (v2.4.0) of WIDesigner.

WIDesigner makes extensive use of XML as its persistence layer (saved instruments,
tunings, etc.). The translation to and from XML is handled by a set of libraries 
(JAXB) that was part of java in versions 8 and lower. Now, WIDesigner incorporates
these libraries as independent inclusions in our distribution. That is why 
WIDesigner jumped from v2.3.0 to v2.4.0.

Java 9 and above introduced a module system which limited the use of JDK internal
calls. This has a direct impact on the UI libraries (kindly supplied by JideSoft)
integral to WIDesigner functionality. In order to appease later java versions, 
runtime parameters must be passed to java when invoking WIDesigner. That is, 
WIDesigner must be started from a command line, batch file, command file, or
script when using a later version of java.

So what does this mean to you? If you are using java 8 or earlier (as I am),
nothing has changed: continue invoking WIDesigner as you have before (typically
by double clicking the jar file). If you are using a later java version, use the
command line that is in run_WIDesigner_java-9.cmd, copying this line to your container
of choice (batch file, command file, or script); for Windows, you can just use
the provided .cmd file directly. Be aware that the command directly calls java;
so java.exe must be in your PATH variable.

The later java versions introduce a slightly different look and feel in regard
to fonts and spacing. If these differences cause you grief, please contact us
through the the Issues page. These are a few additional command-line parameters that
might work for your particular operating system/java version.