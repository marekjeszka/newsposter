package com.jeszka.persistence;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.api.client.util.IOUtils;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class S3DataStoreFactory extends AbstractDataStoreFactory {
    @Autowired
    AmazonS3Client amazonS3Client;
    private String bucketName;

    public S3DataStoreFactory(String bucketName) {
        this.bucketName = bucketName;
    }


    @Override
    protected <V extends Serializable> S3DataStore<V> createDataStore(String id) throws IOException {
        return new S3DataStore<>(this, id);
    }

    class S3DataStore<V extends Serializable> extends AbstractDataStore<V> {
        private static final String FILE_NAME = "credentials_gmail";

        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private HashMap<String, byte[]> keyValueMap = Maps.newHashMap();

        public S3DataStore(S3DataStoreFactory s3DataStoreFactory, String id) throws IOException {
            super(s3DataStoreFactory, id);

            GetObjectRequest request = new GetObjectRequest(bucketName, FILE_NAME);
            final S3Object object;
            try {
                object = amazonS3Client.getObject(request);
                // get credentials from S3 file
                keyValueMap = IOUtils.deserialize(object.getObjectContent());
            } catch (AmazonS3Exception e) {
                // ignore, file does not exist
            }
        }

        @Override
        public Set<String> keySet() throws IOException {
            lock.readLock().lock();
            try {
                return Collections.unmodifiableSet(keyValueMap.keySet());
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public Collection<V> values() throws IOException {
            lock.readLock().lock();
            try {
                List<V> result = Lists.newArrayList();
                for (byte[] bytes : keyValueMap.values()) {
                    result.add(IOUtils.<V>deserialize(bytes));
                }
                return Collections.unmodifiableList(result);
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public V get(String key) throws IOException {
            if (key == null) {
                return null;
            }
            lock.readLock().lock();
            try {
                return IOUtils.deserialize(keyValueMap.get(key));
            } finally {
                lock.readLock().unlock();
            }
        }

        @Override
        public DataStore<V> set(String key, V value) throws IOException {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(value);
            lock.writeLock().lock();
            try {
                keyValueMap.put(key, IOUtils.serialize(value));
                save();
            } finally {
                lock.writeLock().unlock();
            }
            return this;
        }

        @Override
        public DataStore<V> clear() throws IOException {
            lock.writeLock().lock();
            try {
                keyValueMap.clear();
                save();
            } finally {
                lock.writeLock().unlock();
            }
            return this;
        }

        @Override
        public DataStore<V> delete(String key) throws IOException {
            if (key == null) {
                return this;
            }
            lock.writeLock().lock();
            try {
                keyValueMap.remove(key);
                save();
            } finally {
                lock.writeLock().unlock();
            }
            return this;
        }

        private void save() throws IOException {
            final byte[] serializedMap = SerializationUtils.serialize(keyValueMap);
            InputStream inputStream = new ByteArrayInputStream(serializedMap);
            // TODO add meta-data

            PutObjectRequest putRequest = new PutObjectRequest(
                    bucketName,
                    FILE_NAME,
                    inputStream,
                    null);
            final PutObjectResult putObjectResult = amazonS3Client.putObject(putRequest);
            System.out.println("Result storing to S3: " + putObjectResult.getETag());
        }
    }
}
