@echo off
echo Generating Keystore for HTTPS Server...
keytool -genkeypair -alias rsiproject -keyalg RSA -keysize 2048 -storetype JKS -keystore keystore.jks -validity 3650 -storepass password -keypass password -dname "CN=localhost, OU=RSI, O=Politechnika, L=Bialystok, ST=Podlaskie, C=PL"
echo Keystore generated successfully as 'keystore.jks'.
pause
