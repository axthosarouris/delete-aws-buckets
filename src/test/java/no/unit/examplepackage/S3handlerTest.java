package no.unit.examplepackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class S3handlerTest {

    public static final String LINE_SEPARATOR = System.lineSeparator();
    private final S3handler s3handler = new S3handler();


    @Test
    @Tag("online")
    public void listBucketsReturnsAllBucketNames() throws IOException {
         writeToFile(toString(s3handler.listAllBuckets()));
    }

    @Test
    @Tag("online")
    public void deleteBucketsDeletesBuckets(){
        List<String> bucketNames = IoUtils.linesfromResource(Path.of("bucketsToDelete.txt"));
        try{
            bucketNames.forEach(s3handler::emptyAndDeleteBucket);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    private void writeToFile(String input) throws IOException {
        File file = new File("existingBuckets.txt");
        if(file.exists()){
            file.delete();
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        writer.write(input);
        writer.close();

    }

    private String toString(List<String> buckets) {
        return String.join(LINE_SEPARATOR, buckets);
    }
}
