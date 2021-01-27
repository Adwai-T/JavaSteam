# Java Steam Bot

## Add UserDetails

Create a File "UserDetails" in src/login folder. Paste the Code and fill in the details in the class. 

As security saving passwords and sensitive data on file is not good especially if you are going to build on
this project. So we will save our variables as environment variables.

If you are not concerned with security the variables can be directly filled in with relevant String values.

```java
package login;

public class UserDetails {

    public static final String USERNAME = System.getenv("USER");
    public static final String PASSWORD = System.getenv("PASS");
    public static final String APIKEY = System.getenv("KEY");
    public static final String MONGODBLINK = System.getenv("MONGO");
//Set SECRET to null if you don't have shared_secret or will provide the twofactorcode at the time of login.
    public static final String SECRET = System.getenv("SECRET"); 

    public UserDetails() { }
}
```

## Using MongoDB

`mongo-java-driver-3.8.1-javadoc` is used with this project currently.

Download the drivers from this [link](https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.8.1/).

Use `-Djdk.tls.client.protocols=TLSv1.2` as the VM parameters as there is a SSLException with the Mongo Servers recently.

