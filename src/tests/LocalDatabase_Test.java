package tests;

import com.mongodb.client.*;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.Document;

public class LocalDatabase_Test {

    MongoDatabase db;

    public LocalDatabase_Test(MongoClient client) {

        //--- Watch
        this.db = client.getDatabase("Steam");
        MongoCollection<Document> col = db.getCollection("Accept");

        MongoCursor<ChangeStreamDocument<Document>> cursor = col.watch().iterator();

        if(cursor.hasNext()){
            System.out.println(cursor.next());
            System.out.println(cursor.next().getFullDocument());
        }

        //---General operations
//        Document test = new Document();
//        test.append("Name", "Test1");
//        test.append("Value", "1");
//
//        Document test2 = new Document();
//        test2.append("Name", "Test2");
//        test2.append("Value", "2");
//
//        MongoCollection<Document> col_test = db.getCollection("col_test");
//        List<Document> docs = new ArrayList<>();
//        docs.add(test);
//        docs.add(test2);
//        col_test.insertMany(docs);
//
//        Document myDoc = col_test.find().first();
//        System.out.println(myDoc.toJson());
    }
}
