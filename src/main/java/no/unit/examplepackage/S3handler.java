package no.unit.examplepackage;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.util.List;
import java.util.stream.Collectors;

public class S3handler {

    private final AmazonS3 s3Client;

    public S3handler() {
        this.s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).build();
    }

    public List<String> listAllBuckets() {
        return s3Client.listBuckets().stream()
            .map(Bucket::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    public void emptyAndDeleteBucket(String bucketName) {
        emptyBucket(bucketName);
        s3Client.deleteBucket(bucketName);
    }

    public void createBucket(String bucketName){
        s3Client.createBucket(bucketName);
    }

    public void emptyBucket(String bucketName) {
        System.out.println(bucketName);
        VersionListing versionListing = s3Client
            .listVersions(new ListVersionsRequest().withBucketName(bucketName));

        while (versionListing.isTruncated()) {
            deleteVersionBatch(bucketName, s3Client, versionListing);
            versionListing = s3Client.listNextBatchOfVersions(versionListing);
        }
        deleteVersionBatch(bucketName, s3Client, versionListing);

        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while (objectListing.isTruncated()) {
            deleteObjectBatch(bucketName, s3Client, objectListing);
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
        }

        deleteObjectBatch(bucketName, s3Client, objectListing);

        // Be sure we have nothing more to delete
        if (!versionListing.getVersionSummaries().isEmpty() || !objectListing.getObjectSummaries()
            .isEmpty()) {
            emptyBucket(bucketName);
        }
    }
    
    private void deleteObjectBatch(String bucketName, AmazonS3 s3, ObjectListing objectListing) {
        objectListing.getObjectSummaries()
            .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
    }

    private void deleteVersionBatch(String bucketName, AmazonS3 s3, VersionListing versionListing) {
        versionListing.getVersionSummaries()
            .forEach(
                version -> s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
    }
}
