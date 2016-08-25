//----------------------------------------------------------------------------------
// Microsoft Developer & Platform Evangelism
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
// THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
// EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
//----------------------------------------------------------------------------------
// The example companies, organizations, products, domain names,
// e-mail addresses, logos, people, places, and events depicted
// herein are fictitious.  No association with any real company,
// organization, product, domain name, email address, logo, person,
// places, or events is intended or should be inferred.
//----------------------------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.CopyStatus;
import com.microsoft.azure.storage.file.FileRange;
import com.microsoft.azure.storage.file.ListFileItem;

/**
 * This sample illustrates basic usage of the Azure file storage service.
 */
public class FileBasics {

    /**
     * Azure Storage File Sample
     *
     * @throws Exception
     */
    public void runSamples() throws Exception {

        System.out.println("Azure Storage File sample - Starting.");

        CloudFileClient fileClient = null;
        CloudFileShare fileShare1 = null;
        CloudFileShare fileShare2 = null;
        FileInputStream fileInputStream = null;

        try {

            // Create a file client for interacting with the file service
            fileClient = FileClientProvider.getFileClientReference();

            // Create sample file for upload demonstration
            Random random = new Random();
            System.out.println("\nCreating sample file between 128KB-256KB in size for upload demonstration.");
            File tempFile1 = DataGenerator.createTempLocalFile("file-", ".tmp", (128 * 1024) + random.nextInt(256 * 1024));
            System.out.println(String.format("\tSuccessfully created the file \"%s\".", tempFile1.getAbsolutePath()));

            // Create file share with randomized name
            System.out.println("\nCreate file share for the sample demonstration");
            fileShare1 = createFileShare(fileClient, DataGenerator.createRandomName("filebasics-"));
            System.out.println(String.format("\tSuccessfully created the file share \"%s\".", fileShare1.getName()));

            // Get a reference to the root directory of the share.
            CloudFileDirectory rootDir1 = fileShare1.getRootDirectoryReference();

            // Upload a local file to the root directory
            System.out.println("\nUpload the sample file to the root directory.");
            CloudFile file1 = rootDir1.getFileReference(tempFile1.getName());
            file1.uploadFromFile(tempFile1.getAbsolutePath());
            System.out.println("\tSuccessfully uploaded the file.");

            // Create a random directory under the root directory
            System.out.println("\nCreate a random directory under the root directory");
            CloudFileDirectory dir = rootDir1.getDirectoryReference(DataGenerator.createRandomName("dir-"));
            if (dir.createIfNotExists()) {
                System.out.println(String.format("\tSuccessfully created the directory \"%s\".", dir.getName()));
            }
            else {
                throw new IllegalStateException(String.format("Directory with name \"%s\" already exists.", dir.getName()));
            }

            // Upload a local file to the newly created directory sparsely (Only upload certain ranges of the file)
            System.out.println("\nUpload the sample file to the newly created directory partially in distinct ranges.");
            CloudFile file1sparse = dir.getFileReference(tempFile1.getName());
            file1sparse.create(tempFile1.length());
            fileInputStream = new FileInputStream(tempFile1);
            System.out.println("\t\tRange start: 0, length: 1024.");
            file1sparse.uploadRange(fileInputStream, 0, 1024);
            System.out.println("\t\tRange start: 4096, length: 1536.");
            fileInputStream.getChannel().position(4096);
            file1sparse.uploadRange(fileInputStream, 4096, 1536);
            System.out.println("\t\tRange start: 8192, length: EOF.");
            fileInputStream.getChannel().position(8192);
            file1sparse.uploadRange(fileInputStream, 8192, tempFile1.length() - 8192);
            fileInputStream.close();
            System.out.println("\tSuccessfully uploaded the file sparsely.");

            // Query the file ranges
            System.out.println(String.format("\nQuery the file ranges for \"%s\".", file1sparse.getUri().toURL()));
            ArrayList<FileRange> fileRanges = file1sparse.downloadFileRanges();
            for (Iterator<FileRange> itr = fileRanges.iterator(); itr.hasNext(); ) {
                FileRange fileRange = itr.next();
                System.out.println(String.format("\tStart offset: %d, End offset: %d", fileRange.getStartOffset(), fileRange.getEndOffset()));
            }

            // Clear a range and re-query the file ranges
            System.out.println(String.format("\nClearing the second range partially and then re-query the file ranges for \"%s\".", file1sparse.getUri().toURL()));
            file1sparse.clearRange(4608, 512);
            fileRanges = file1sparse.downloadFileRanges();
            for (Iterator<FileRange> itr = fileRanges.iterator(); itr.hasNext(); ) {
                FileRange fileRange = itr.next();
                System.out.println(String.format("\tStart offset: %d, End offset: %d", fileRange.getStartOffset(), fileRange.getEndOffset()));
            }

            // Create another file share with randomized name
            System.out.println("\nCreate another file share for the sample demonstration");
            fileShare2 = createFileShare(fileClient, DataGenerator.createRandomName("filebasics-"));
            System.out.println(String.format("\tSuccessfully created the file share \"%s\".", fileShare2.getName()));

            // Get a reference to the root directory of the share.
            CloudFileDirectory rootDir2 = fileShare2.getRootDirectoryReference();

            // Create sample file for copy demonstration
            System.out.println("\nCreating sample file between 10MB-15MB in size for copy demonstration.");
            File tempFile2 = DataGenerator.createTempLocalFile("file-", ".tmp", (10 * 1024 * 1024) + random.nextInt(5 * 1024 * 1024));
            System.out.println(String.format("\tSuccessfully created the file \"%s\".", tempFile2.getAbsolutePath()));

            // Upload a local file to the root directory
            System.out.println("\nUpload the sample file to the root directory.");
            CloudFile file2 = rootDir1.getFileReference(tempFile2.getName());
            file2.uploadFromFile(tempFile2.getAbsolutePath());
            System.out.println("\tSuccessfully uploaded the file.");

            // Copy the file between shares
            System.out.println(String.format("\nCopying file \"%s\" from share \"%s\" into the share \"%s\".", file2.getName(), fileShare1.getName(), fileShare2.getName()));
            CloudFile file2copy = rootDir2.getFileReference(file2.getName() + "-copy");
            file2copy.startCopy(file2);
            waitForCopyToComplete(file2copy);
            System.out.println("\tSuccessfully copied the file.");

            // Abort copying the file between shares
            System.out.println(String.format("\nAbort when copying file \"%s\" from share \"%s\" into the share \"%s\".", file2.getName(), fileShare1.getName(), fileShare2.getName()));
            System.out.println(String.format("\nAbort when copying file from the root directory \"%s\" into the directory we created \"%s\".", file2.getUri().toURL(), dir.getUri().toURL()));
            CloudFile file2copyaborted = rootDir2.getFileReference(file2.getName() + "-copyaborted");
            boolean copyAborted = true;
            String copyId = file2copyaborted.startCopy(file2);
            try {
                file2copyaborted.abortCopy(copyId);
            }
            catch (StorageException ex) {
                if (ex.getErrorCode().equals("NoPendingCopyOperation")) {
                    copyAborted = false;
                } else {
                    throw ex;
                }
            }
            if (copyAborted == true) {
                System.out.println("\tSuccessfully aborted copying the file.");
            } else {
                System.out.println("\tFailed to abort copying the file because the copy finished before we could abort.");
            }

            // List all file shares and files/directories in each share
            System.out.println("\nList all file shares and files/directories in each share.");
            enumerateFileSharesAndContents(fileClient);

            // Download the uploaded files
            System.out.println("\nDownload the uploaded files.");
            String downloadedFilePath = String.format("%s%s", System.getProperty("java.io.tmpdir"), file1.getName());
            System.out.println(String.format("\tDownload the fully uploaded file from \"%s\" to \"%s\".", file1.getUri().toURL(), downloadedFilePath));
            file1.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            downloadedFilePath = String.format("%s%s", System.getProperty("java.io.tmpdir"), file1sparse.getName());
            System.out.println(String.format("\tDownload the sparsely uploaded file from \"%s\" to \"%s\".", file1sparse.getUri().toURL(), downloadedFilePath));
            file1sparse.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            downloadedFilePath = String.format("%s%s", System.getProperty("java.io.tmpdir"), file2.getName());
            System.out.println(String.format("\tDownload the copied file from \"%s\" to \"%s\".", file2.getUri().toURL(), downloadedFilePath));
            file2.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            downloadedFilePath = String.format("%s%s", System.getProperty("java.io.tmpdir"), file2copy.getName());
            System.out.println(String.format("\tDownload the copied file from \"%s\" to \"%s\".", file2copy.getUri().toURL(), downloadedFilePath));
            file2copy.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            downloadedFilePath = String.format("%s%s", System.getProperty("java.io.tmpdir"), file2copyaborted.getName());
            System.out.println(String.format("\tDownload the copied file from \"%s\" to \"%s\".", file2copyaborted.getUri().toURL(), downloadedFilePath));
            file2copyaborted.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            System.out.println("\tSuccessfully downloaded the files.");

            // Delete the files and directory
            System.out.print("\nDelete the filess and directory. Press any key to continue...");

            file1.delete();
            file1sparse.delete();
            file2.delete();
            file2copy.delete();
            file2copyaborted.delete();
            System.out.println("\tSuccessfully deleted the files.");
            dir.delete();
            System.out.println("\tSuccessfully deleted the directory.");
        }
        catch (Throwable t) {
            PrintHelper.printException(t);
        }
        finally {
            // Delete any file shares that we created (If you do not want to delete the file share comment the line of code below)
            System.out.print("\nDelete any file shares we created.");

            if (fileShare1 != null && fileShare1.deleteIfExists() == true) {
                System.out.println(String.format("\tSuccessfully deleted the file share: %s", fileShare1.getName()));
            }

            if (fileShare2 != null && fileShare2.deleteIfExists() == true) {
                System.out.println(String.format("\tSuccessfully deleted the file share: %s", fileShare2.getName()));
            }

            // Close the file input stream of the local temporary file
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        System.out.println("\nAzure Storage File sample - Completed.\n");
    }

    /**
     * Creates and returns a file share for the sample application to use.
     *
     * @param fileShareName Name of the file share to create
     * @return The newly created CloudFileShare object
     *
     * @throws StorageException
     * @throws RuntimeException
     * @throws IOException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     */
    private static CloudFileShare createFileShare(CloudFileClient fileClient, String fileShareName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // Create a new file share
        CloudFileShare fileShare = fileClient.getShareReference(fileShareName);
        try {
            if (fileShare.createIfNotExists() == false) {
                throw new IllegalStateException(String.format("File share with name \"%s\" already exists.", fileShareName));
            }
        }
        catch (StorageException s) {
            if (s.getCause() instanceof java.net.ConnectException) {
                System.out.println("Caught connection exception from the client. If running with the default configuration please make sure you have started the storage emulator.");
            }
            throw s;
        }

        return fileShare;
    }

    /**
     * Enumerates the contents of the file share.
     *
     * @param rootDir Root directory which needs to be enumerated
     *
     * @throws StorageException
     */
    private static void enumerateDirectoryContents(CloudFileDirectory rootDir) throws StorageException {

        Iterable<ListFileItem> results = rootDir.listFilesAndDirectories();
        for (Iterator<ListFileItem> itr = results.iterator(); itr.hasNext(); ) {
            ListFileItem item = itr.next();
            boolean isDirectory = item.getClass() == CloudFileDirectory.class;
            System.out.println(String.format("\t\t%s: %s", isDirectory ? "Directory " : "File      ", item.getUri().toString()));
            if (isDirectory == true) {
            	enumerateDirectoryContents((CloudFileDirectory) item);
            }
        }
    }

    /**
     * Enumerates the shares and contents of the file shares.
     *
     * @param fileClient CloudFileClient object
     *
     * @throws StorageException
     * @throws URISyntaxException
     */
    private static void enumerateFileSharesAndContents(CloudFileClient fileClient) throws StorageException, URISyntaxException {

        for (CloudFileShare share : fileClient.listShares("filebasics")) {
            System.out.println(String.format("\tFile Share: %s", share.getName()));
            enumerateDirectoryContents(share.getRootDirectoryReference());
        }
    }

    /**
     * Wait until the copy complete.
     *
     * @param file Target of the copy operation
     *
     * @throws InterruptedException
     * @throws StorageException
     */
    private static void waitForCopyToComplete(CloudFile file) throws InterruptedException, StorageException {
        CopyStatus copyStatus = CopyStatus.PENDING;
        while (copyStatus == CopyStatus.PENDING) {
            Thread.sleep(1000);
            copyStatus = file.getCopyState().getStatus();
        }
    }
}
