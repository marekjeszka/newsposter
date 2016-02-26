package com.jeszka.persistence;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class S3DataStoreFactoryTest {
    @Mock
    AmazonS3Client amazonS3Client;

    @InjectMocks
    S3DataStoreFactory s3DataStoreFactory = new S3DataStoreFactory("bucket");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void created() throws IOException {
        when(amazonS3Client.getObject(any())).thenThrow(AmazonS3Exception.class);

        final S3DataStoreFactory.S3DataStore<Serializable> dataStore = s3DataStoreFactory.createDataStore("id");

        assertNotNull(dataStore);
    }
}
