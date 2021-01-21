# Java Steam Bot

## Add UserDetails

Create a File "UserDetails" in src/login folder.

Paste the Code and fill in the details in the class.

```java
package login;

public class UserDetails {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    public UserDetails() {}
}
```

## Using MongoDB

`mongo-java-driver-3.8.1-javadoc` is used with this project currently.

Download the drivers from this [link](https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.8.1/).

Use `-Djdk.tls.client.protocols=TLSv1.2` as the VM parameters as there is a SSLException with the Mongo Servers recently.

