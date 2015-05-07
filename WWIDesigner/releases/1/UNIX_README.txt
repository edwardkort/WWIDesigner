For those running WIDesigner on a UNIX or Linux system, you will
need to do a bit of setup.

If you are running in a graphic shell, in order to invoke the
program by double clicking the jar file, you will need to change
its permissions. chmod a+x should do the trick.

If you want to run it from the command line, the invocation is:
	java -jar WIDesigner-<version #>.jar
This assumes java is in you PATH and you are running in the
WIDesigner-1.0 directory. And you can, of course, include the
above command line in your favorite shell scripting language.