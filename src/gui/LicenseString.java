package gui;

public class LicenseString {
    public static final String APP_NAME = "Rain MIDI";
    public static final String APP_VERSION = "1.07";
    public static final String APP_YEAR = "2025";
    public static final String APP_COMPANY = "Suka";

    private static final String headerLinks = """
<div style='margin-bottom:12px'>
    <a href='jpn'>日本語</a> | <a href='eng'>English</a>
</div>
""";

    private static final String snsLinks = """
<ul>
  <li><a href="https://youtube.com/@sukao_o567?si=EvGffBqJNjGzoMVd">Youtube</a></li>
  <li><a href="https://x.com/Suka_XxO_OxX">X</a></li>
  <li><a href="https://github.com/Suka-T">GitHub</a></li>
</ul>
""";

    private static final String css = """
<style>
  body {
    font-family: "Consolas", "Courier New", monospace;
    background-color: #fafafa;
    color: #222;
    margin: 16px;
  }
  h1 {
    font-size: 20px;
    background-color: #4a90e2;
    color: white;
    padding: 8px 12px;
    border-radius: 6px;
  }
  h2 {
    background-color: #e0e0e0;
    padding: 6px 10px;
    border-radius: 4px;
    border-left: 4px solid #888;
    font-size: 14px;
    margin-top: 24px;
  }
  pre {
    background-color: #f4f4f4;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 10px;
    white-space: pre-wrap;
    font-size: 10px;
  }
  a {
    color: #0077cc;
    text-decoration: none;
  }
  a:hover {
    text-decoration: underline;
  }
</style>
""";

    public static final String htmlJpn = """
<html>
  <head>
    <meta charset="UTF-8">
    """ + css + """
</head>

<body>
""" + "<h1>" + APP_NAME + " - v" + APP_VERSION + "</h1>" + headerLinks + """
<h2>著作権表示</h2>
<pre>
""" + "Copyright (c) " + APP_YEAR + ", " + APP_COMPANY + "<br>" + "All rights reserved."
+ """
</pre>
""" + snsLinks
+ """
<h2>使用許諾</h2>
<pre>
※以下の条件に従う限り、このソフトウェアを使用できます。

1. このソフトウェアはフリーソフトウェアです。
個人使用に関わらず自由に使用してかまいません。

2. 複製、再配布を行う場合は、上記の著作権表示、
配布物とともに提供される文書を必ず含めること。

3. 動作環境や、プログラムの不具合などによって問題が生じる場合があります。
それらを原因とした損害が発生しても、著作権者は一切の責任を取りません。
</pre>

<h2>Zulu JDK/JREについて</h2>
<pre>
このアプリケーションには、Azul Systems, Inc. により提供される
Zulu OpenJDK が含まれています。

Zulu OpenJDK は、GNU General Public License version 2（GPLv2）
および Classpath Exception のもとでライセンスされています。
</pre>

<ul>
  <li>OpenJDKプロジェクト: <a href="https://openjdk.java.net/">https://openjdk.java.net/</a></li>
  <li>GPLv2 + Classpath Exception: <a href="https://openjdk.java.net/legal/gplv2+ce.html">https://openjdk.java.net/legal/gplv2+ce.html</a></li>
  <li>Azul Systems: <a href="https://www.azul.com/">https://www.azul.com/</a></li>
</ul>

<pre>
本アプリケーションに含まれる Zulu JDK/JRE のソースコードは、
Azul にリクエストすることで入手可能です
（受領から3年間以内、実費のみ）。

詳細は azul_openjdk@azul.com にお問い合わせください。

使用しているJDK/JREのバージョン：
Zulu 21.42+19 (c2f88d00-0d1c-448f-8f72-27f1326b7e18)
    </pre>
  </body>
</html>
""";

    public static final String htmlEng = """
<html>
  <head>
    <meta charset="UTF-8">
    """ + css + """
</head>

<body>
""" + "<h1>" + APP_NAME + " - v" + APP_VERSION + "</h1>" + headerLinks + """
<h2>Copyright</h2>
<pre>
""" + "Copyright (c) " + APP_YEAR + ", " + APP_COMPANY + "<br>" + "All rights reserved."
+ """
""" + snsLinks
+ """
</pre>
<h2>License</h2>
<pre>
You may use this software under the following conditions:

1. This software is free software.
You may use it freely for personal or commercial purposes.

2. If you redistribute or copy this software,
you must include the above copyright notice
and the accompanying documentation.

3. The author is not responsible for any damage
caused by the software, including malfunction
or environment issues.
</pre>

<h2>About Zulu JDK/JRE</h2>
<pre>
This application includes Zulu OpenJDK provided
by Azul Systems, Inc.

Zulu OpenJDK is licensed under the GNU General Public
License version 2 (GPLv2) with the Classpath Exception.
</pre>

<ul>
  <li>OpenJDK Project: <a href="https://openjdk.java.net/">https://openjdk.java.net/</a></li>
  <li>GPLv2 + Classpath Exception: <a href="https://openjdk.java.net/legal/gplv2+ce.html">https://openjdk.java.net/legal/gplv2+ce.html</a></li>
  <li>Azul Systems: <a href="https://www.azul.com/">https://www.azul.com/</a></li>
</ul>

<pre>
The source code of Zulu JDK/JRE included in this application
can be obtained by requesting it from Azul
(available within 3 years from receipt, actual cost only).

For details, contact azul_openjdk@azul.com.

JDK/JRE version used:
Zulu 21.42+19 (c2f88d00-0d1c-448f-8f72-27f1326b7e18)
    </pre>
  </body>
</html>
""";

}
