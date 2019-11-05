# Installation

1. Install [git-secret](https://git-secret.io/)
2. Add your public key to repo keychain
3. Add pre-commit hook `$ git config core.hooksPath .githooks`
4. Reveal secrets `$ git secret reveal`
5. Run 
``` 
sudo keytool -import \
    -alias jbcert \
    -keystore /usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts \
    -file jetbrainsCA.crt \
    -storepass changeit \
    -noprompt
```