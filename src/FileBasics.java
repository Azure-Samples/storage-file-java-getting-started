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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
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
    protected final static String tempFileNamePrefix = "HelloWorld-";
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
        try {
            // Create a scanner for user input
            scan = new Scanner(System.in);

            // Create a sample file for use
            System.out.println("Creating a sample file for upload demonstration.");
            File tempFile = File.createTempFile(tempFileNamePrefix, tempFileNameSuffix);
            bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            for (int i = 0; i < 256; i++) {
                bufferedWriter.write("Hello World!!");
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
            System.out.println(String.format("\n2. Upload the sample file \"%s\" to the root directory.", tempFile.getAbsolutePath()));
            CloudFile fileUnderRootDir = rootDir.getFileReference(tempFile.getName());
            fileUnderRootDir.uploadFromFile(tempFile.getAbsolutePath());
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

            // Upload a local file to the newly created directory
            System.out.println(String.format("\n4. Upload the sample file \"%s\" to the newly created directory.", tempFile.getAbsolutePath()));
            CloudFile file = dir.getFileReference(tempFile.getName());
            file.uploadFromFile(tempFile.getAbsolutePath());
            System.out.println("\tSuccessfully uploaded the file.");

            // List all files/directories under the root directory
            System.out.println("\n5. List Files/Directories in root directory.");
            Iterable<ListFileItem> results = rootDir.listFilesAndDirectories();
            for (Iterator<ListFileItem> itr = results.iterator(); itr.hasNext(); ) {
                ListFileItem item = itr.next();
                String itemType = "FILE";
                if (item.getClass() == CloudFileDirectory.class) {
                    itemType = "DIR";
                }
                System.out.println(String.format("\t%s\t: %s", itemType, item.getUri().toString()));
            }

            // Download the uploaded file
            String downloadedImagePath = String.format("%sCopyOf-%s", System.getProperty("java.io.tmpdir"), tempFile.getName());
            System.out.println(String.format("\n6. Download file from \"%s\" to \"%s\".", file.getUri().toURL(), downloadedImagePath));
            file.downloadToFile(downloadedImagePath);
            System.out.println("\tSuccessfully downloaded the file.");

            // Delete the file and directory
            System.out.print("6. Delete the file and directory. Press any key to continue...");
            scan.nextLine();
            file.delete();
            System.out.println("\tSuccessfully deleted the file.");
            dir.delete();
            System.out.println("\tSuccessfully deleted the directory.");
        }
        catch (Throwable t) {
            printException(t);
        }
        finally {
            // Delete the file share (If you do not want to delete the file share comment the line of code below)
            System.out.print("\n7. Delete the file share. Press any key to continue...");
            scan.nextLine();
            deleteFileShare(fileShare);

            // Close the buffered writer
            bufferedWriter.close();

            // Close the scanner
            scan.close();
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
     * Delete the specified file share.
     *
     * @param table The {@link CloudFileShare} object to delete
     *
     * @throws StorageException
     */
    private static void deleteFileShare(CloudFileShare fileShare) throws StorageException {

        if (fileShare != null && fileShare.deleteIfExists() == true) {
            System.out.println("\tSuccessfully deleted the file share.");
        }
        else {
            System.out.println("\tNothing to delete.");
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
