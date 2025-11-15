# A=B Java interpreter 

该项目为 esolang`A=B`的`JAVA`解释器实现.

A=B是一门只有一条指令的编程语言：A=B，意思是将A替换为B。

### 命令行参数

- `-c, --cli`: 忽略参数运行(改为在命令行请求)
- `-d, --detail`: 需要详细输出
- `-fio, --using-file-io`: 使用文件输入测试样例(`程序文件名_io.txt`)
- `-i, --file <路径>`: 指定程序文件输入路径
- `-o, --output <路径>`: 指定日志输出路径(为空默认为`程序文件名.log`)
- `-t, --time <最大执行行数>`: 最大执行行数(默认`1000`)

> 例: `java -jar AeqB.jar -d -fio -o "" -i “sample_cases\code2.txt” -t 200`

本项目实现的A=B指令集包括：
- `A = B` 将A替换为B
- `once start end return` 拓展关键字

本项目支持预编写输入输出调试。  
这是一个练习项目，不涉及商业使用，侵删。 
> 灵感来源: [Steam 上的 A=B](https://store.steampowered.com/app/1720850) 