# ExtractFunctionsOfBinaries
This script is used with Ghidra allowing to extract functions of binaries.

# How to use
After generating ghidra.jar with buildGhidraJar script, you can type this command :
```
java -Xmx1024M -jar ghidra.jar path/to/ghidra_dir ghidra_dir_name -import binary_file -scriptPath path/to/scripts_dir -postScript ExtractFunctions.java
```
Tested with ghidra 9.1.2 and 9.2.
