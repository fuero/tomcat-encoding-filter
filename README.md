# Encoding filter for a Shibboleth-protected Tomcat

## License

Copyright [2015] [Robert Führicht <robert.fuehricht@jku.at>]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License [here][apl2]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Purpose

Shibboleth SP provides its attributes to Apache in UTF-8.
Sadly, if they are passed on to Tomcat, it [defaults to
ISO 8859-1][bytechunk] and doesn't enumerate passed AJP attributes 
(when using `ShibUseEnvironment On`)

[AJP 1.3][ajp13] [doesn't][ajp13a] specify which encoding its strings should be in, so it relies
on HTTP. The [RFC][rfc7230] covering this isn't conclusive:

> Historically, HTTP has allowed field content with text in the
> ISO-8859-1 charset [ISO-8859-1], supporting other charsets only
> through use of [RFC2047] encoding.  In practice, most HTTP header
> field values use only a subset of the US-ASCII charset [USASCII].
> Newly defined header fields SHOULD limit their field values to
> US-ASCII octets.  A recipient SHOULD treat other octets in field
> content (obs-text) as opaque data.

Furthermore, it fixes getAttributeNames(), which doesn't yield *anything*
when using AJP.

## Usage

Add this to your `web.xml`

    <filter>
      <filter-name>my-encoding-filter</filter-name>
      <filter-class>at.jku.filters.SetCharacterEncodingFilter</filter-class>
      <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
      </init-param>
    </filter>
     
    <filter-mapping>
      <filter-name>my-encoding-filter</filter-name>
      <url-pattern>/*</url-pattern>
    </filter-mapping>

## Acknowledgements

SetCharacterEncodingFilter was taken and modified from the Tomcat 7.0.x tree.

[bytechunk]: http://svn.apache.org/repos/asf/tomcat/tc7.0.x/trunk/java/org/apache/tomcat/util/buf/ByteChunk.java "ByteChunk.java"
[apl2]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License 2.0"
[rfc7230]: https://tools.ietf.org/html/rfc7230#section-3.2.4 "RFC 7230 - HTTP/1.1 Message Syntax and Routing, Section 3.2.4"
[ajp13]: http://tomcat.apache.org/tomcat-3.3-doc/AJPv13.html "AJP 1.3"
[ajp13a]: http://tomcat.apache.org/connectors-doc/ajp/ajpv13a.html "Tomcat docs - AJP 1.3"
