set CLASSPATH=C:\Development\WWIDesigner\WWIDesigner\class;C:\Development\WWIDesigner\WWIDesigner\lib\commons-math-2.2.jar
rem javac -d class src\main\com\wwidesigner\impedance\util\*.java
rem javac -d class src\main\com\wwidesigner\impedance\note\*.java
javac -d class -sourcepath src\main\com\wwidesigner\impedance\math src\main\com\wwidesigner\impedance\math\StateVector.java
javac -d class -sourcepath src\main\com\wwidesigner\impedance\math src\main\com\wwidesigner\impedance\math\TransferMatrix.java
rem javac -d class src\main\com\wwidesigner\impedance\geometry\*.java
rem javac -d class src\main\com\wwidesigner\impedance\math\ImpedanceSpectrum.java
rem javac -d class src\main\com\wwidesigner\impedance\geometry\Instrument.java
rem javac -d class src\main\com\wwidesigner\impedance\gui\*.java
