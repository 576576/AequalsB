# A=B  Java interpreter 

esolang `A=B`后，我萌生了用Java试着写一个其解释器的想法，这就是本仓库A=B的由来。
本项目实现了A=B的拓展指令集，包括：
- string1=string2 将string1替换为string2
- 关键字 once start end return
-----
缺陷：
- 代码及输入特殊字符(*+等)报错 *替换成不特殊字符即可*
- testcase合法性未经检查 *懒得检查*
-----
本项目支持testcase.txt进行预编写调试。  
这是一个练习项目，不涉及商业使用，侵删。 
