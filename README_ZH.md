*计算机管理员的一个小伎俩*

**警告:我不对任何隐私侵犯带来的纠纷负责**

**使用这个程序意味着你承担所有可能的后果**


这个工具一旦被部署到一台电脑上, 新插入的可移动存储器便会被扫描, 然后指定文件被复制到你的电脑上的一个固定位置.

# 编译所需的依赖
* [libPaperCube-JavaSE](https://github.com/PaperCube/libPaperCube-JavaSE)
* Kotlin
* Junit 4 (用于测试)

# 自定义设置
数据存储在C:\ProgramData\Local\FileSpy中。macOS尚未支持，但是可以通过少数的一些修改使之支持。
最重要的自定义设置是文件名匹配的正则表达式。它存储在数据目录下的patterns.txt中，一行一个。