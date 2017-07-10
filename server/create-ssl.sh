#!/bin/sh


keytool -genkey -keystore keystore.ssl -alias keyname -keyalg RSA -keysize 4096 -validity 3650

cat <<EOF
Add to server.xml:
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true" scheme="https" secure="true" compression="on"
           clientAuth="false" sslProtocol="TLS"
           keyAlias="keyname" keystoreFile="keystore.ssl"
           keystorePass="PASSWORD" />
EOF