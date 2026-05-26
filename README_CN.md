<p align="center">
  <b>中文</b> | <a href="./README.md">English</a>
</p>

<p align="center">
  <img src="https://img.shields.io/github/license/onlyGuo/dnf_parser?style=flat-square&color=green" alt="License" />
  <img src="https://img.shields.io/github/last-commit/onlyGuo/dnf_parser?style=flat-square&logo=github&color=purple" alt="Last Commit" />
  <img src="https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17+" />
  <a href="https://search.maven.org/artifact/ink.icoding.dnf/dnf-parser">
    <img src="https://img.shields.io/maven-central/v/ink.icoding.dnf/dnf-parser?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven Central" />
  </a>
  <img src="https://img.shields.io/badge/DNF-NPK%20%26%20PVF-5b7fff?style=flat-square" alt="DNF NPK and PVF" />
</p>

<h1 align="center">dnf-parser</h1>

<p align="center">
  <b>用于解析 Dungeon & Fighter NPK 与 PVF 资源的 Java 工具库</b>
</p>

<p align="center">
  在 JVM 上读取 DNF 资源文件，包括 NPK 图像资源和 PVF 脚本。<br/>
  本仓库还修复了 PVF 中元素文本错乱 / 不对应的问题，尤其是名称、描述等字段错误命中 <code>n_string</code> 的情况。
</p>

<p align="center">
  <a href="https://github.com/onlyGuo/dnf_parser/stargazers"><img src="https://img.shields.io/github/stars/onlyGuo/dnf_parser?style=social" alt="GitHub Stars" /></a>
  &nbsp;
  <a href="https://github.com/onlyGuo/dnf_parser/network/members"><img src="https://img.shields.io/github/forks/onlyGuo/dnf_parser?style=social" alt="GitHub Forks" /></a>
  &nbsp;
  <a href="https://github.com/onlyGuo/dnf_parser/watchers"><img src="https://img.shields.io/github/watchers/onlyGuo/dnf_parser?style=social" alt="GitHub Watchers" /></a>
</p>

---

## 项目简介

`dnf-parser` 是一个 Java 多模块项目，用于读取 DNF 资源文件：

- `dnf-parser-core`：公共工具和底层辅助能力
- `dnf-parser-npk`：解析 NPK 资源包及 IMG 资源
- `dnf-parser-pvf`：解析 PVF 文件树、字符串表和脚本内容

项目基于 Java 17，使用 Maven 进行依赖管理与构建。

## 项目来源说明

本仓库基于原作者在 Gitee 上发布的项目继续整理与修复：

- 原始仓库：`https://gitee.com/cn595980161/dnf_parser`

由于 GitHub 上没有原作者对应的同步仓库，因此这里新建了一个 GitHub 仓库，便于继续维护、使用与发布。

在原作者实现的基础上，本仓库重点修复了 PVF 文本解析错乱的问题：某些元素的名称、描述等文本字段，过去会因为 `n_string` 在全局范围内提前命中错误的 `.str` 文件，导致文本内容不对应。现在改为按当前脚本的文件前缀进行匹配，从而定位到正确的文本来源。

## 功能特性

- 解析 DNF `NPK` 资源
- 解析 DNF `PVF` 文件树与脚本
- 以 JSON 形式读取结构化 PVF 脚本
- 通过 `loadScriptSource(...)` 读取重建后的 PVF 脚本文本
- 按文件前缀精确匹配 `n_string`，避免元素文本错乱 / 不对应

## 环境要求

- Java 17+
- 推荐 Maven 3.9+

## 安装方式

如果制品已发布到你的 Maven 仓库中，可以直接引入以下依赖。

### NPK 解析模块

```xml
<dependency>
    <groupId>ink.icoding.dnf</groupId>
    <artifactId>dnf-parser-npk</artifactId>
    <version>1.0</version>
</dependency>
```

### PVF 解析模块

```xml
<dependency>
    <groupId>ink.icoding.dnf</groupId>
    <artifactId>dnf-parser-pvf</artifactId>
    <version>1.0</version>
</dependency>
```

本地构建：

```bash
mvn clean package
```

## 使用示例

### 解析 NPK 图片资源

```java
import com.xiaoyouma.dnf.parser.npk.coder.NpkCoder;
import com.xiaoyouma.dnf.parser.npk.model.NpkImg;

public class Demo {
    public static void main(String[] args) {
        NpkCoder.initialize("D:/dnf/dof/DOF/ImagePacks2");
        NpkImg npkImg = NpkCoder.loadImg(
                "sprite/character/swordman/equipment/avatar/skin/sm_body0000.img"
        );
        System.out.println(npkImg);
    }
}
```

### 以 JSON 形式解析 PVF 脚本

```java
import cn.hutool.json.JSONObject;
import com.xiaoyouma.dnf.parser.pvf.coder.PvfCoder;

import java.nio.charset.Charset;

public class Demo {
    public static void main(String[] args) {
        PvfCoder.initialize("/path/to/Script.pvf", Charset.forName("Big5"));
        JSONObject script = PvfCoder.loadScript("stackable/book_skill2.stk");
        System.out.println(script);
    }
}
```

### 读取重建后的 PVF 脚本原文

```java
import com.xiaoyouma.dnf.parser.pvf.coder.PvfCoder;

import java.nio.charset.Charset;

public class Demo {
    public static void main(String[] args) {
        PvfCoder.initialize("/path/to/Script.pvf", Charset.forName("Big5"));
        String source = PvfCoder.loadScriptSource("stackable/book_skill2.stk");
        System.out.println(source);
    }
}
```

## 关于 PVF 文本修复

原先的 `Pvf#getNString(...)` 会在全局 `n_string` 集合中拿到第一个匹配结果，因此当多个 `.str` 文件存在相同 key 时，就可能出现错误命中。例如：

- 当前脚本是 `stackable/book_skill2.stk`
- 正确文本应来自 `stackable/stackable.kor.str`
- 但旧逻辑可能提前命中 `dungeon/dungeon.kor.str`

本仓库已修复为按当前脚本路径前缀匹配，因此名称、描述等文本字段会更准确地对应到正确来源。

## 开源协议

本项目基于 GNU GPL v3 协议发布，详见 [`LICENSE`](./LICENSE)。


