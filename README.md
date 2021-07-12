
## JWT Codec - A JSON Web Token tool
JWT Codec is a simple, practical GUI tool that helps you easily encode/sign and
decode/verify [JSON Web Tokens (JWTs)](https://jwt.io/) with several
algorithms. It can automatically generate random cryptographic keys to sign your
JWTs; if you prefer, you can also use the
[.pem files](https://en.wikipedia.org/wiki/Privacy-Enhanced_Mail) you already
have. JWT Codec supports signing and verifying JWTs with the following
algorithms:

- HMAC: HS256, HS384 and HS512;
- RSASSA-PKCS1-v1_5: RS256, RS384 and RS512;
- ECDSA: ES256, ES384 and ES512;
- RSASSA-PSS: PS256, PS384 and PS512.

It can also generate random cryptographic keys in the following formats:

- HMAC SHA-256, SHA-384 and SHA-512;
- RSA 2048-bit, 3072-bit and 4096-bit;
- EC 256-bit, 384-bit and 521-bit.

JWT Codec leverages the excellent
[FusionAuth JWT](https://www.github.com/fusionauth/fusionauth-jwt) library. To
run this tool, you only need an installation of [Java 8 or later][link-jdk]. 

### Building
To build JWT Codec, you need [JDK 8 or later][link-jdk],
[Maven](https://maven.apache.org/) and [Ant](https://ant.apache.org/). Execute
the commands below in the project directory:

```bash
$ mvn clean package
$ ant
```

This will build the tool and copy distribution files into the `dist/`
subdirectory. If you would like to modify or customize the Windows `.exe`
launcher, use [Launch4j](http://launch4j.sourceforge.net/) to open the `.xml`
file in the `launch4j/` subdirectory, make the desired changes and regenerate
the `.exe` file to your liking. Enjoy!

[link-jdk]: https://www.adoptopenjdk.net/
