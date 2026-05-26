<p align="center">
  <a href="./README_CN.md">中文</a> | <b>English</b>
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
  <b>Java parsers for Dungeon & Fighter NPK and PVF resources</b>
</p>

<p align="center">
  Read DNF asset files on the JVM, including NPK images and PVF scripts.<br/>
  This repository also fixes PVF text mismatches where element names/descriptions could resolve to incorrect <code>n_string</code> entries.
</p>

<p align="center">
  <a href="https://github.com/onlyGuo/dnf_parser/stargazers"><img src="https://img.shields.io/github/stars/onlyGuo/dnf_parser?style=social" alt="GitHub Stars" /></a>
  &nbsp;
  <a href="https://github.com/onlyGuo/dnf_parser/network/members"><img src="https://img.shields.io/github/forks/onlyGuo/dnf_parser?style=social" alt="GitHub Forks" /></a>
  &nbsp;
  <a href="https://github.com/onlyGuo/dnf_parser/watchers"><img src="https://img.shields.io/github/watchers/onlyGuo/dnf_parser?style=social" alt="GitHub Watchers" /></a>
</p>

---

## Overview

`dnf-parser` is a Java multi-module project for reading DNF resource files:

- `dnf-parser-core`: shared utilities and low-level helpers
- `dnf-parser-npk`: parses NPK asset packages and IMG resources
- `dnf-parser-pvf`: parses PVF trees, string tables, and script files

The project targets Java 17 and uses Maven for dependency management and packaging.

## Project Background

This repository is based on the original work published by the original author on Gitee:

- Original repository: `https://gitee.com/cn595980161/dnf_parser`

There was no corresponding GitHub repository maintained by that author, so this GitHub repository was created to make the project easier to use, maintain, and publish from GitHub.

On top of the original implementation, this repository includes a fix for PVF text resolution issues: some element text fields such as names and descriptions could previously resolve to the wrong `n_string` source because lookup returned the first global match instead of matching the correct file prefix.

## Features

- Parse DNF `NPK` resources
- Parse DNF `PVF` directory trees and scripts
- Read structured PVF script data as JSON
- Read reconstructed PVF script source text via `loadScriptSource(...)`
- Correct `n_string` lookup using file-prefix-aware matching to avoid text mismatches

## Requirements

- Java 17+
- Maven 3.9+ recommended

## Installation

If the artifacts are available from your Maven repository, add one of the following dependencies.

### NPK parser

```xml
<dependency>
    <groupId>ink.icoding.dnf</groupId>
    <artifactId>dnf-parser-npk</artifactId>
    <version>1.0</version>
</dependency>
```

### PVF parser

```xml
<dependency>
    <groupId>ink.icoding.dnf</groupId>
    <artifactId>dnf-parser-pvf</artifactId>
    <version>1.0</version>
</dependency>
```

To build locally:

```bash
mvn clean package
```

## Usage

### Parse an NPK image

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

### Parse a PVF script as JSON

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

### Read reconstructed PVF script source

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

## Notes on PVF Text Fixes

In the original behavior, `Pvf#getNString(...)` could return the first matching key from the global `n_string` file set. That meant fields like item names or descriptions could match a wrong `.str` file when the same key existed in multiple domains.

This repository fixes that by matching `n_string` entries with the current script file prefix, for example:

- `stackable/book_skill2.stk` -> only match `stackable/...*.str`
- instead of stopping on an unrelated earlier match like `dungeon/dungeon.kor.str`

This makes element text fields line up with their expected source dictionary more reliably.

## License

This project is distributed under the GNU GPL v3. See [`LICENSE`](./LICENSE) for details.
