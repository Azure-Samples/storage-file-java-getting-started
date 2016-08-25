/*
  Copyright Microsoft Corporation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;

/**
 * This sample illustrates advanced usage of the Azure file storage service.
 */
class FileAdvanced {

    /**
     * Executes the samples.
     *
     * @throws URISyntaxException Uri has invalid syntax
     * @throws InvalidKeyException Invalid key
     */
    void runSamples() throws InvalidKeyException, URISyntaxException, IOException {
        System.out.println();
        System.out.println();
        PrintHelper.printSampleStartInfo("File Advanced");

        // Create a file service client
        CloudFileClient fileClient = FileClientProvider.getFileClientReference();

        try {
            System.out.println("Service properties sample");
            serviceProperties(fileClient);
            System.out.println();

            System.out.println("Share properties sample");
            shareProperties(fileClient);
            System.out.println();

            System.out.println("Share metadata sample");
            shareMetadata(fileClient);
            System.out.println();

            System.out.println("Directory properties sample");
            directoryProperties(fileClient);
            System.out.println();

            System.out.println("Directory metadata sample");
            directoryMetadata(fileClient);
            System.out.println();

            System.out.println("File properties sample");
            fileProperties(fileClient);
            System.out.println();

            System.out.println("File metadata sample");
            fileMetadata(fileClient);
            System.out.println();
        }
        catch (Throwable t) {
            PrintHelper.printException(t);
        }

        PrintHelper.printSampleCompleteInfo("File Advanced");
    }

    /**
     * Manage the service properties including hour and minute metrics.
     * @param fileClient Azure Storage File Service
     */
    private void serviceProperties(CloudFileClient fileClient) throws StorageException {
        System.out.println("Get service properties");
        FileServiceProperties originalProps = fileClient.downloadServiceProperties();

        try {
            System.out.println("Set service properties");
            // Change service properties
            FileServiceProperties props = new FileServiceProperties();

            final MetricsProperties hours = props.getHourMetrics();
            hours.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
            hours.setRetentionIntervalInDays(1);
            hours.setVersion("1.0");

            final MetricsProperties minutes = props.getMinuteMetrics();
            minutes.setMetricsLevel(MetricsLevel.SERVICE);
            minutes.setRetentionIntervalInDays(1);
            minutes.setVersion("1.0");

            fileClient.uploadServiceProperties(props);

            System.out.println();
            System.out.println("Hour Metrics");
            System.out.printf("version: %s%n", props.getHourMetrics().getVersion());
            System.out.printf("retention interval: %d%n", props.getHourMetrics().getRetentionIntervalInDays());
            System.out.printf("operation types: %s%n", props.getHourMetrics().getMetricsLevel());
            System.out.println();
            System.out.println("Minute Metrics");
            System.out.printf("version: %s%n", props.getMinuteMetrics().getVersion());
            System.out.printf("retention interval: %d%n", props.getMinuteMetrics().getRetentionIntervalInDays());
            System.out.printf("operation types: %s%n", props.getMinuteMetrics().getMetricsLevel());
            System.out.println();
        }
        finally {
            // Revert back to original service properties
            fileClient.uploadServiceProperties(originalProps);
        }
    }

    /**
     * Manage Share Properties
     * 
     * @param fileClient Azure Storage File Service
     */
    private void shareProperties(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        // Create share
        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);

        try {
            System.out.println("Create share");
            fileShare.createIfNotExists();

            // Set share properties
            System.out.println("Set share properties");
            fileShare.getProperties().setShareQuota(10);
            fileShare.uploadProperties();

            // Get share properties
            System.out.println("Get share properties");
            FileShareProperties props = fileShare.getProperties();

            System.out.println();
            System.out.printf("share quota: %s%n", props.getShareQuota());
            System.out.printf("last modified: %s%n", props.getLastModified());
            System.out.printf("Etag: %s%n", props.getEtag());
            System.out.println();
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.deleteIfExists();
        }
    }

    /**
     * Manage Share Metadata
     *
     * @param fileClient Azure Storage File Service
     */
    private void shareMetadata(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);
        try {
            // Set share metadata
            System.out.println("Set share metadata");
            HashMap<String, String> metadata = new HashMap<>();
            metadata.put("key1", "value1");
            metadata.put("foo", "bar");
            fileShare.setMetadata(metadata);

            // Create share
            System.out.println("Create share");
            fileShare.createIfNotExists();

            // Get share metadata
            System.out.println("Get share metadata");
            metadata = fileShare.getMetadata();
            Iterator it = metadata.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.printf(" %s = %s%n", pair.getKey(), pair.getValue());
                it.remove();
            }
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.deleteIfExists();
        }
    }

    /**
     * Get Directory Properties
     *
     * @param fileClient Azure Storage File Service
     */
    private void directoryProperties(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);
        try {
            // Create share
            System.out.println("Create share");
            fileShare.createIfNotExists();

            // Create the root directory of the share.
            System.out.println("Create root directory");
            CloudFileDirectory rootDir = fileShare.getRootDirectoryReference();
            rootDir.createIfNotExists();

            // Get directory properties
            System.out.println("Get root directory properties");
            FileDirectoryProperties props = rootDir.getProperties();

            System.out.println();
            System.out.printf("last modified: %s%n", props.getLastModified());
            System.out.printf("Etag: %s%n", props.getEtag());
            System.out.println();
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.deleteIfExists();
        }
    }

    /**
     * Manage Directory Metadata
     *
     * @param fileClient Azure Storage File Service
     */
    private void directoryMetadata(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        // Create share
        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);
        try {
            System.out.println("Create share");
            fileShare.createIfNotExists();

            CloudFileDirectory rootDir = fileShare.getRootDirectoryReference();

            // Set directory metadata
            System.out.println("Set directory metadata");
            HashMap<String, String> metadata = new HashMap<>();
            metadata.put("key1", "value1");
            metadata.put("foo", "bar");
            rootDir.setMetadata(metadata);

            // Create the root directory of the share.
            System.out.println("Create root directory");
            rootDir.createIfNotExists();

            // Get directory metadata
            System.out.println("Get directory metadata");
            metadata = rootDir.getMetadata();
            Iterator it = metadata.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.printf(" %s = %s%n", pair.getKey(), pair.getValue());
                it.remove();
            }
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.deleteIfExists();
        }
    }

    /**
     * Manage file properties
     * @param fileClient Azure Storage File Service
     */
    private void fileProperties(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);
        try {
            // Create share
            System.out.println("Create share");
            fileShare.createIfNotExists();

            // Create the root directory of the share.
            System.out.println("Create root directory");
            CloudFileDirectory rootDir = fileShare.getRootDirectoryReference();
            rootDir.createIfNotExists();

            // Get a reference to a file
            CloudFile file = rootDir.getFileReference("file"
                    + UUID.randomUUID().toString().replace("-", ""));

            // Set file properties
            System.out.println("Set file properties");
            file.getProperties().setContentLanguage("en");
            file.getProperties().setContentType("text/plain");
            file.getProperties().setContentEncoding("UTF-8");

            // Create the file
            System.out.println("Create file");
            file.create(10);

            // Get file properties
            System.out.println("Get file properties");
            FileProperties props = file.getProperties();

            System.out.println();
            System.out.printf("last modified: %s%n", props.getLastModified());
            System.out.printf("cache control: %s%n", props.getCacheControl());
            System.out.printf("content type: %s%n", props.getContentType());
            System.out.printf("content language: %s%n", props.getContentLanguage());
            System.out.printf("content encoding: %s%n", props.getContentEncoding());
            System.out.printf("content disposition: %s%n", props.getContentDisposition());
            System.out.printf("content MD5: %s%n", props.getContentMD5());
            System.out.printf("Length: %s%n", props.getLength());
            System.out.printf("Copy state: %s%n", props.getCopyState());
            System.out.println();
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.delete();
        }
    }

    /**
     * Manage file metadata
     * @param fileClient Azure Storage File Service
     */
    private void fileMetadata(CloudFileClient fileClient) throws URISyntaxException, StorageException {

        String fileShareName = DataGenerator.createRandomName("share-");
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);

        try {
            // Create share
            System.out.println("Create share");
            fileShare.createIfNotExists();

            // Create the root directory of the share.
            System.out.println("Create root directory");
            CloudFileDirectory rootDir = fileShare.getRootDirectoryReference();
            rootDir.createIfNotExists();

            // Get a reference to a file
            CloudFile file = rootDir.getFileReference("file"
                    + UUID.randomUUID().toString().replace("-", ""));

            // Set file metadata
            System.out.println("Set file metadata");
            HashMap<String, String> metadata = new HashMap<>();
            metadata.put("key1", "value1");
            metadata.put("foo", "bar");
            file.setMetadata(metadata);

            // Create the file
            System.out.println("Create file");
            file.create(10);

            // Get file metadata
            System.out.println("Get file metadata:");
            metadata = file.getMetadata();
            Iterator it = metadata.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.printf(" %s = %s%n", pair.getKey(), pair.getValue());
                it.remove();
            }
        }
        finally {
            // Delete share
            System.out.println("Delete share");
            fileShare.delete();
        }
    }
}
