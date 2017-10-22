For Chinese version, see [here](README_ZH.md)

*A useful tool for computer administrators*

**Note that I don't take any responsibilities for privacy violations.**

**You agree to be responsible for all aftermaths using this program.**


Start this program, insert your removable drive, then everything will be scanned and some files will be copied to your computer. This is especially useful when you know your professors often store the exam results in his USB flash disk, but they never give result to anyone though you are strongly desiring one. Since then, when they insert their flash disks into the computer in your classroom for PPTs, you can everything you want.

# Dependencies of compilation
* [libPaperCube-JavaSE](https://github.com/PaperCube/libPaperCube-JavaSE)
* Kotlin

# Customizations and data storage
Everything including stolen files and settings are stored in C:\ProgramData\Local\FileSpy. MacOS is currently unsupported while several modifications are needed to make it work on it.
The FileSpy decides whether a file needs to be copied or not by checking its name with Regular Expressions in `patterns.txt`. One line for one pattern.

Enjoy.
