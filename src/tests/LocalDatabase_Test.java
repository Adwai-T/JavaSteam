package tests;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class LocalDatabase_Test {

    public LocalDatabase_Test(MongoClient client) {
        MongoDatabase db = client.getDatabase("Steam");

        Document test = new Document();
        test.append("Name", "Test1");
        test.append("Value", "1");

        Document test2 = new Document();
        test2.append("Name", "Test2");
        test2.append("Value", "2");

        MongoCollection<Document> col_test = db.getCollection("col_test");
        List<Document> docs = new ArrayList<>();
        docs.add(test);
        docs.add(test2);
        col_test.insertMany(docs);

        Document myDoc = col_test.find().first();
        System.out.println(myDoc.toJson());
    }
}
