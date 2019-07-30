For those running WIDesigner on a UNIX or Linux system, you will
need to do a bit of setup.

If you are running in a graphic shell, to enable invoking the
program by double clicking its jar file, you will need to change
the jar file permissions. chmod a+x should do the trick.

If you want to run WIDesigner from the command line, the invocation is:

	java -jar WIDesigner-<version #>.jar

This assumes java is in your PATH and the WIDesigner-2.3 directory
is your current working directory. You can, of course, use the
above command line in your favorite shell scripting language.