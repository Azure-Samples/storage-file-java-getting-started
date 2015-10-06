---
services: storage
platforms: java
author: sribhat-MSFT
---

# Getting Started with Azure File Service in Java

Azure File Service Sample - Demonstrates how to perform common tasks using the Microsoft Azure File Service.

The Azure File service exposes file shares using the standard SMB protocol. Applications running in Azure can now easily share files between VMs using standard and familiar file system APIs like ReadFile and WriteFile. In addition, the files can also be accessed at the same time via a REST interface, which opens a variety of hybrid scenarios. Azure Files is built on the same technology as the Blob, Table, and Queue Services, which means Azure Files is able to leverage the existing availability, durability, scalability, and geo redundancy that is built into the Azure platform.

Note: If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

## Running this sample

This sample can be run using your Azure Storage account by updating the config.properties file with your "AccountName" and "Key".

To run the sample using the Azure Storage File Service:

1. Create a Storage Account through the Azure Portal and provide your account name and account key in the config.properties file.
2. Set breakpoints and run the project.

## More information

[What is a Storage Account](http://azure.microsoft.com/en-us/documentation/articles/storage-whatis-account/)

[Getting Started with Files](http://blogs.msdn.com/b/windowsazurestorage/archive/2014/05/12/introducing-microsoft-azure-file-service.aspx)

[File Service Concepts](http://msdn.microsoft.com/en-us/library/dn166972.aspx)

[File Service REST API](http://msdn.microsoft.com/en-us/library/dn167006.aspx)

[Azure Storage Java API](http://azure.github.io/azure-storage-java/)

