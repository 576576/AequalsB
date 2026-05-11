# A=B Java interpreter

[![Java](https://img.shields.io/badge/java-25-blue.svg)](https://openjdk.org/projects/jdk/25/)
[![Maven](https://img.shields.io/badge/maven-3.9%2B-green.svg)](https://maven.apache.org)

A=B 是一门极简深奥编程语言（esolang）的 Java解释器。该语言只有一条基本指令：`A = B`，含义为「将字符串中的 A 替换为 B」。本项目实现了其包含修饰符的拓展指令集。

> **v2.0** 重构版：Maven + picocli + JUnit 5，Records 封装，管道式不可变对象。

---

## 目录

- [语言规范](#语言规范)
- [快速开始](#快速开始)
- [CLI 参考](#cli-参考)
- [示例程序](#示例程序)
- [项目结构](#项目结构)
- [开发](#开发)
- [致谢](#致谢)

---

## 语言规范

### 指令语法

```
[修饰符] 模式 = [修饰符] 替换文本
```

每条指令由等号 `=` 分隔的**模式**和**替换文本**组成。程序从上到下扫描，匹配后回到第一行重新开始。

### 关键字修饰符

| 修饰符 | 位置 | 作用 |
|--------|------|------|
| `(start)` | 左侧 | 仅匹配**开头** |
| `(end)` | 左侧 | 仅匹配**结尾** |
| `(once)` | 左侧 | **仅执行一次**，之后跳过 |
| `(start)` | 右侧 | 移除匹配文本后**前置** |
| `(end)` | 右侧 | 移除匹配文本后**后置** |
| `(return)` | 右侧 | 替换后**立即终止** |

### 注释

- `#` 开头的行视为注释
- 程序源码中的空格会自动忽略

---

## 快速开始

### 前置条件

- [JDK 25](https://jdk.java.net/25/)
- [Maven](https://maven.apache.org) 3.9+

### 构建

```bash
cd AequalsB
mvn package -DskipTests
java -jar target/AequalsB-2.0.0.jar -i sample_cases/code1.txt --fio -d
```

### 运行

```bash
# 详细模式 + 默认测试用例
java -jar target/AequalsB-2.0.0.jar --fio -d

# 交互模式 + 加载示例程序1
java -jar target/AequalsB-2.0.0.jar -i sample_cases/code1.txt -c

# 交互模式 + 指定时间限制
java -jar target/AequalsB-2.0.0.jar -c -t 100
```

---

## CLI 参考

```
Usage: AeqB [-hV] [-c] [-d] [--fio] [-i=<file>] [-o=<output>] [-t=<time>]

  -c, --cli            忽略参数，在终端中交互式输入
  -d, --detail         详细输出（显示每步执行日志）
      --fio            使用文件测试用例（<程序>_io.txt）
  -i, --file=<file>    指定 A=B 程序文件路径（.txt）
  -o, --output=<output> 日志输出路径（默认 <程序>.log）
  -t, --time=<time>    最大执行步数（默认 1000）
```

---

## 示例程序

### 简单替换

`sample_cases/code1.txt`：

| 输入 | 输出            | 说明 |
|------|---------------|------|
| `aaabbababa` | `hello world` | b→a 后匹配 aaaa，return 结束 |
| `aba` | `sayonara`    | b→a 后无匹配，空模式 =666 追加 |
| `vvv` | `sayonara`    | vvv= 移除，=666 追加触发 666666 |

### 二进制乘法

`sample_cases/code2.txt` 实现了完整的二进制乘法：

| 输入 | 输出 | 数学含义 |
|------|------|----------|
| `11*1` | `11` | 3 × 1 = 3 |
| `101*10` | `1010` | 5 × 2 = 10 |
| `11*11` | `1001` | 3 × 3 = 9 |

---

## 项目结构

```
AequalsB/
├── pom.xml                              # Maven 构建（picocli, JUnit 5, AssertJ）
├── README.md
├── sample_cases/
│   ├── code1.txt / code1_io.txt         # 简单替换样例
│   └── code2.txt / code2_io.txt         # 二进制乘法样例
└── src/
    ├── main/java/io/github/aeqb/
    │   ├── Main.java                    # 入口：picocli @Command (Callable<Integer>)
    │   ├── Engine.java                  # 核心引擎：封装实例
    │   ├── Parser.java                  # 程序解析器：String -> List<Rule>
    │   ├── Utils.java                   # 工具类：括号匹配、关键字提取、校验
    │   ├── Keyword.java                 # enum: START/END/ONCE/RETURN
    │   ├── Rule.java                    # record: 单条规则
    │   ├── StepLog.java                 # record: 执行步骤日志
    │   └── ExecutionResult.java         # record: output + log + timedOut
    └── test/java/io/github/aeqb/
        ├── ParserTest.java              # 解析器单元测试
        ├── EngineTest.java              # 引擎单元测试
        └── IntegrationTest.java         # 集成测试 — 样例用例
```

---

## 开发

```bash
# 运行全部测试
mvn test

# 仅运行单元测试
mvn test -Dtest="ParserTest,EngineTest"

# 仅运行集成测试
mvn test -Dtest="IntegrationTest"

# 打包可执行 JAR
mvn package
```

---

## 致谢

- **灵感来源**：[A=B on Steam](https://store.steampowered.com/app/1720850)
- **Rust 移植版**：[AeqB_rust](../AeqB_rust)
