# Java Steam Bot

## External Dependencies

* `json-simple-1.1.1` : Parse Json Response from the server.
* `mongo-java-driver-3.8.1-javadoc` : Mongodb Drivers for interaction with Database. Save Trades to database, also get items to trade and values.

## Add UserDetails

Create a File "UserDetails" in src/login folder. Paste the Code and fill in the details in the class. 

As security saving passwords and sensitive data on file is not good especially if you are going to build on
this project. So we will save our variables as environment variables.

If you are not concerned with security the variables can be directly filled in with relevant String values.

```java
package login;

/*
 * API Key is necessary for all the main.java.trade related information.
 * Login to get the Auth details is possible without the API key.
 */

public class UserDetails {

    public static final String STEAMID = System.getenv("ID"); //Optional
    public static final String USERNAME = System.getenv("USER"); //Required
    public static final String PASSWORD = System.getenv("PASS"); //Required
    public static final String APIKEY = System.getenv("KEY"); //Required
    public static final String MONGODBLINK = System.getenv("MONGO"); //Set to null if not provided
    public static final String SHAREDSECRET = System.getenv("SECRET"); //Set to null if not provided
    public static final String IDENTITYSECRET = System.getenv("IDENTITY"); //Set to null if not provided

    public UserDetails() { }
}
```

## Using MongoDB

`mongo-java-driver-3.8.1-javadoc` is used with this project currently.

Download the drivers from this [link](https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.8.1/).

Use `-Djdk.tls.client.protocols=TLSv1.2` as the VM parameters as there is a SSLException with the Mongo Servers recently.

## Sensitive data

When ever we `Login` to steam Files named `Cookies.json` and `TransferParamters.json` are created. These files are very
important as they contain all the Cookies and Authentication data that steam sends us after Login successfully.

This data that we save can be used directly for current session trades and confirmations as well as any future trades. It
will be used till the data remains valid.

If the data becomes invalid the bot will automatically try to Re-Login provided you have populated all fields in UserDetails file.
Especially the SharedSecret as it will be used to generate Mobile Authentication Code.

>Note : If you use my bot **protecting your Credentials and sensitive data that is saved is your responsibility.**
>
>If you clone/fork the bot do add the files to gitignore if you make changes and use it in your project be careful with
>these generated files.
>
>I also recommend that the UserDetails file be kept as is and all the Credentials be provided as Environment Variables.
>
>If you get banned which is always a possibility, I will in no way be able to help. Steam's bot detection can might detect
>the bot and temporarily prevent you from requesting, but there might be more serious consequences.
>
>Lastly Item trade is not always perfect as Item description provided by steam is not always consistent, and the bot might
>not be able to recognize the item perfectly.