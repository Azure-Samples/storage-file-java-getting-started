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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.azure.storage.file.FileRange;
import com.microsoft.azure.storage.file.ListFileItem;

/*
 * Azure File Service Sample - Demonstrate how to perform common tasks using the Microsoft Azure File Service.
 *
 * Documentation References:
 *  - What is a Storage Account - http://azure.microsoft.com/en-us/documentation/articles/storage-whatis-account/
 *  - Getting Started with Files - http://blogs.msdn.com/b/windowsazurestorage/archive/2014/05/12/introducing-microsoft-azure-file-service.aspx
 *  - How to use Azure File Storage - http://azure.microsoft.com/en-us/documentation/articles/storage-dotnet-how-to-use-files/
 *  - File Service Concepts - http://msdn.microsoft.com/en-us/library/dn166972.aspx
 *  - File Service REST API - http://msdn.microsoft.com/en-us/library/dn167006.aspx
 *  - Azure Storage Java API - http://azure.github.io/azure-storage-java/
 *
 * Instructions:
 *      This sample can be run using your Azure Storage account by updating the config.properties file with your "AccountName" and "Key".
 *
 *      To run the sample using the Storage Service
 *          1.  Create a Storage Account through the Azure Portal and provide your [AccountName] and [AccountKey] in the config.properties file.
 *              See https://azure.microsoft.com/en-us/documentation/articles/storage-create-storage-account/ for more information.
 *          2.  Set breakpoints and run the project.
 */
public class FileBasics {

    protected static CloudFileShare fileShare = null;
    protected final static String fileShareNamePrefix = "filebasics";
    protected final static String directoryNamePrefix = "dir-";
    protected final static String tempFileNamePrefix = "file-";
    protected final static String tempFileNameSuffix = ".txt";

    /**
     * Azure Storage File Sample
     *
     * @param args No input arguments are expected from users.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("Azure Storage File sample - Starting.\n");

        Scanner scan = null;
        BufferedWriter bufferedWriter = null;
        FileInputStream fileInputStream = null;
        try {
            // Create a scanner for user input
            scan = new Scanner(System.in);

            // Create a sample file for use
            System.out.println("Creating a sample file for upload demonstration.");
            File tempFile = File.createTempFile(tempFileNamePrefix, tempFileNameSuffix);
            tempFile.deleteOnExit();
            bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            for (int i = 0; i < 1024; i++) {
                bufferedWriter.write(UUID.randomUUID().toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            System.out.println(String.format("\tSuccessfully created the file \"%s\".", tempFile.getAbsolutePath()));

            // Create new file share with a randomized name
            String fileShareName = fileShareNamePrefix + UUID.randomUUID().toString().replace("-", "");
            System.out.println(String.format("\n1. Create a file share with name \"%s\".", fileShareName));
            try {
                fileShare = createFileShare(fileShareName);
            }
            catch (IllegalStateException e) {
                System.out.println(String.format("\tFileShare already exists."));
                throw e;
            }
            System.out.println("\tSuccessfully created the file share.");

            // Get a reference to the root directory of the share.
            CloudFileDirectory rootDir = fileShare.getRootDirectoryReference();

            // Upload a local file to the root directory
            System.out.println("\n2. Upload the sample file to the root directory.");
            CloudFile file1 = rootDir.getFileReference(tempFile.getName());
            file1.uploadFromFile(tempFile.getAbsolutePath());
            System.out.println("\tSuccessfully uploaded the file.");

            // Create a random directory under the root directory
            String directoryName = directoryNamePrefix + UUID.randomUUID().toString().replace("-", "");
            System.out.println(String.format("\n3. Creating a random directory \"%s\" under the root directory.", directoryName));
            CloudFileDirectory dir = rootDir.getDirectoryReference(directoryName);
            if (dir.createIfNotExists()) {
                System.out.println("\tSuccessfully created the directory.");
            }
            else {
                System.out.println("\tDirectory already exists.");
                throw new IllegalStateException(String.format("Directory with name \"%s\" already exists.", directoryName));
            }

            // Upload a local file to the newly created directory sparsely (Only upload certain ranges of the file)
            System.out.println("\n4. Upload the sample file to the newly created directory partially in distinct ranges.");
            CloudFile file2 = dir.getFileReference(tempFile.getName());
            file2.create(tempFile.length());
            fileInputStream = new FileInputStream(tempFile);
            System.out.println("\t\tRange start: 0, length: 1024.");
            file2.uploadRange(fileInputStream, 0, 1024);
            System.out.println("\t\tRange start: 4096, length: 1536.");
            fileInputStream.getChannel().position(4096);
            file2.uploadRange(fileInputStream, 4096, 1536);
            System.out.println("\t\tRange start: 8192, length: EOF.");
            fileInputStream.getChannel().position(8192);
            file2.uploadRange(fileInputStream, 8192, tempFile.length() - 8192);
            fileInputStream.close();
            System.out.println("\tSuccessfully uploaded the file sparsely.");

            // Query the file ranges
            System.out.println(String.format("\n5. Query the file ranges for \"%s\".", file2.getUri().toURL()));
            ArrayList<FileRange> fileRanges = file2.downloadFileRanges();
            for (Iterator<FileRange> itr = fileRanges.iterator(); itr.hasNext(); ) {
                FileRange fileRange = itr.next();
                System.out.println(String.format("\tStart offset: %d, End offset: %d", fileRange.getStartOffset(), fileRange.getEndOffset()));
            }

            // Clear a range and re-query the file ranges
            System.out.println(String.format("\n6. Clearing the second range partially and then re-query the file ranges for \"%s\".", file2.getUri().toURL()));
            file2.clearRange(4608, 512);
            fileRanges = file2.downloadFileRanges();
            for (Iterator<FileRange> itr = fileRanges.iterator(); itr.hasNext(); ) {
                FileRange fileRange = itr.next();
                System.out.println(String.format("\tStart offset: %d, End offset: %d", fileRange.getStartOffset(), fileRange.getEndOffset()));
            }

            // List all files/directories under the root directory
            System.out.println("\n7. List Files/Directories in root directory.");
            enumerateFileShare(rootDir);

            // Download the uploaded files
            System.out.println("\n8. Download the uploaded files.");
            String downloadedFilePath = String.format("%sfull-%s", System.getProperty("java.io.tmpdir"), tempFile.getName());
            System.out.println(String.format("\tDownload the fully uploaded file from \"%s\" to \"%s\".", file1.getUri().toURL(), downloadedFilePath));
            file1.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            downloadedFilePath = String.format("%ssparse-%s", System.getProperty("java.io.tmpdir"), tempFile.getName());
            System.out.println(String.format("\tDownload the sparsely uploaded file from \"%s\" to \"%s\".", file2.getUri().toURL(), downloadedFilePath));
            file2.downloadToFile(downloadedFilePath);
            new File(downloadedFilePath).deleteOnExit();
            System.out.println("\tSuccessfully downloaded the files.");

            // Delete the file and directory
            System.out.print("\n9. Delete the files and directory. Press any key to continue...");
            scan.nextLine();
            file1.delete();
            file2.delete();
            System.out.println("\tSuccessfully deleted the files.");
            dir.delete();
            System.out.println("\tSuccessfully deleted the directory.");
        }
        catch (Throwable t) {
            printException(t);
        }
        finally {
            // Delete the file share (If you do not want to delete the file share comment the line of code below)
            if (fileShare != null)
            {
                System.out.print("\n10. Delete the file share. Press any key to continue...");
                scan.nextLine();
                if (fileShare.deleteIfExists() == true) {
                    System.out.println("\tSuccessfully deleted the file share.");
                }
                else {
                    System.out.println("\tNothing to delete.");
                }
            }

            // Close the buffered writer
            if (bufferedWriter!= null) {
                bufferedWriter.close();
            }

            // Close the file input stream of the local temporary file
            if (fileInputStream != null) {
                fileInputStream.close();
            }

            // Close the scanner
            if (scan != null) {
                scan.close();
            }
        }

        System.out.println("\nAzure Storage File sample - Completed.\n");
    }

    /**
     * Validates the connection string and returns the storage account.
     * The connection string must be in the Azure connection string format.
     *
     * @param storageConnectionString Connection string for the storage service or the emulator
     * @return The newly created CloudStorageAccount object
     *
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws InvalidKeyException
     */
    private static CloudStorageAccount getStorageAccountFromConnectionString(String storageConnectionString) throws IllegalArgumentException, URISyntaxException, InvalidKeyException {

        CloudStorageAccount storageAccount;
        try {
            storageAccount = CloudStorageAccount.parse(storageConnectionString);
        }
        catch (IllegalArgumentException|URISyntaxException e) {
            System.out.println("\nConnection string specifies an invalid URI.");
            System.out.println("Please confirm the connection string is in the Azure connection string format.");
            throw e;
        }
        catch (InvalidKeyException e) {
            System.out.println("\nConnection string specifies an invalid key.");
            System.out.println("Please confirm the AccountName and AccountKey in the connection string are valid.");
            throw e;
        }

        return storageAccount;
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
    private static CloudFileShare createFileShare(String fileShareName) throws StorageException, RuntimeException, IOException, InvalidKeyException, IllegalArgumentException, URISyntaxException, IllegalStateException {

        // Retrieve the connection string
        Properties prop = new Properties();
        try {
            InputStream propertyStream = FileBasics.class.getClassLoader().getResourceAsStream("config.properties");
            if (propertyStream != null) {
                prop.load(propertyStream);
            }
            else {
                throw new RuntimeException();
            }
        } catch (RuntimeException|IOException e) {
            System.out.println("\nFailed to load config.properties file.");
            throw e;
        }
        String storageConnectionString = prop.getProperty("StorageConnectionString");

        // Retrieve storage account information from connection string.
        CloudStorageAccount storageAccount = getStorageAccountFromConnectionString(storageConnectionString);

        // Create a file client for interacting with the file service
        CloudFileClient fileShareClient = storageAccount.createCloudFileClient();

        // Create a new file share
        CloudFileShare fileShare = fileShareClient.getShareReference(fileShareName);
        try {
            if (fileShare.createIfNotExists() == false) {
                throw new IllegalStateException(String.format("File share with name \"%s\" already exists.", fileShareName));
            }
        }
        catch (StorageException e) {
            System.out.println("\nCaught storage exception from the client.");
            System.out.println("If running with the default configuration please make sure you have started the storage emulator.");
            throw e;
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
    private static void enumerateFileShare(CloudFileDirectory rootDir) throws StorageException {

        Iterable<ListFileItem> results = rootDir.listFilesAndDirectories();
        for (Iterator<ListFileItem> itr = results.iterator(); itr.hasNext(); ) {
            ListFileItem item = itr.next();
            String itemType = "FILE";
            if (item.getClass() == CloudFileDirectory.class) {
                itemType = "DIR";
            }
            System.out.println(String.format("\t%s\t: %s", itemType, item.getUri().toString()));
            if (item.getClass() == CloudFileDirectory.class) {
                enumerateFileShare((CloudFileDirectory) item);
            }
        }
    }

    /**
     * Print the exception stack trace
     *
     * @param ex Exception to be printed
     */
    public static void printException(Throwable ex) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        System.out.println(String.format("Exception details:\n%s\n", stringWriter.toString()));
    }
}
