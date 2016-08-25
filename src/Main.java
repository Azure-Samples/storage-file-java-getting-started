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
public class Main {

    /**
     * Executes the sample.
     *
     * @param args
     *            No input args are expected from users.
     */
    public static void main(String[] args) throws Exception {
        FileBasics basicSamples = new FileBasics();
        basicSamples.runSamples();

        FileAdvanced advancedSamples = new FileAdvanced();
        advancedSamples.runSamples();
    }
}