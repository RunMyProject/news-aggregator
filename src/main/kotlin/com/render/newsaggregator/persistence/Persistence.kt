// news-aggregator
// @May - 2023 - Edoardo Sabatini

// This package contains classes related to storage data
package com.render.newsaggregator.persistence

import java.io.*
import java.time.*

/**
 * This object provides functions to save and load data from files using Java serialization.
 */
object Persistence {

    private const val TAG = "Persistence news-aggregator"
    private val DEFAULT_LOCK_TIMEOUT = Duration.ofMinutes(30)

    /**
     * Loads data of type [T] from the specified file using Java serialization.
     * Returns null if the file is not found or an error occurs while reading from it.
     *
     * @param fileName the name of the file to read from
     * @return a list of data of type [T], or null if an error occurs
     */
    fun <T> loadData(fileName: String): List<T>? {
        try {
            // Create file input stream and object input stream for the specified file
            FileInputStream(fileName).use { fileInputStream ->
                ObjectInputStream(fileInputStream).use { objectInputStream ->
                    // Read the object from the input stream and cast it to a list of T
                    return objectInputStream.readObject() as List<T>
                }
            }
        } catch (e: FileNotFoundException) {
            // Log error message and return null if file not found
            println("$TAG Error: File $fileName not found")
            return null
        } catch (e: IOException) {
            // Log error message and stack trace and return null if error occurs while reading from file
            println("$TAG Error while loading data from file: $fileName")
            e.printStackTrace()
            return null
        } catch (e: ClassNotFoundException) {
            // Log error message and stack trace and return null if class not found while reading from file
            println("$TAG Error while loading data from file: $fileName")
            e.printStackTrace()
            return null
        }
    }

    /**
     * Saves the specified data to the specified file using Java serialization.
     * Throws an exception if an error occurs while writing to the file.
     *
     * @param data the data to save
     * @param fileName the name of the file to save to
     */
    fun <T> saveData(data: List<T>?, fileName: String) {
        try {
            // Create file output stream and object output stream for the specified file
            FileOutputStream(fileName).use { fileOutputStream ->
                ObjectOutputStream(fileOutputStream).use { objectOutputStream ->
                    // Write the data to the output stream
                    objectOutputStream.writeObject(data)
                }
            }
        } catch (e: IOException) {
            // Log error message and stack trace if error occurs while writing to file
            println("$TAG Error while saving data to file: $fileName")
            e.printStackTrace()
        }
    }

    /**
     * Checks if a file is currently locked by another process.
     *
     * @param fileName the name of the file to check
     * @return true if the file is not locked, or false otherwise
     */
    fun checkFileLock(fileName: String): Boolean {
        val file = File(fileName)

        // If file does not exist, return true
        if (!file.exists()) {
            return true
        }

        val lastModified = Instant.ofEpochMilli(file.lastModified())
        val durationSinceLastModified = Duration.between(lastModified, Instant.now())

        // If file is not locked, return true
        if (durationSinceLastModified >= DEFAULT_LOCK_TIMEOUT) {
            return true
        }

        // If file is locked, return false
        return false
    }

}