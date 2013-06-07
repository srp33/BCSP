// Copyright 2011 Stephen Piccolo
// 
// This file is part of ML-Flex.
// 
// ML-Flex is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// any later version.
// 
// ML-Flex is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with ML-Flex. If not, see <http://www.gnu.org/licenses/>.

package mlflex;

import java.io.*;
import java.util.ArrayList;

/** This class provides utility methods for reading, writing, updating, and deleting files.
 * @author Stephen Piccolo
 */
public class Files
{
    /** Indicates the age (in minutes) of a file.
     *
     * @param filePath Absolute file path
     * @return Age in minutes
     * @throws Exception
     */
    public static double GetFileAgeMinutes(String filePath) throws Exception
    {
        return Dates.DifferenceInMinutes(Dates.GetCurrentDate(), Dates.CreateDate(new File(filePath).lastModified()));
    }

    /** Indicates the number of non-blank lines in a file.
     *
     * @param filePath Absolute file path
     * @return Number of lines
     * @throws Exception
     */
    public static int GetNumLinesInFile(String filePath) throws Exception
    {
        if (!Files.FileExists(filePath))
            return 0;
        
        return ReadLinesFromFile(filePath).size();
    }

    /** Convenience method that appends text to an existing file.
     *
     * @param filePath Absolute file path
     * @param text Text to append
     * @throws Exception
     */
    public static void AppendTextToFile(String filePath, String text) throws Exception
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        out.write(text);
        out.close();
    }

    /** Appends a line to an existing file (incuding a new line character).
     *
     * @param filePath Absolute file path
     * @param text Text to append
     * @throws Exception
     */
    public static void AppendLineToFile(String filePath, String text) throws Exception
    {
        AppendTextToFile(filePath, text + "\n");
    }

    /** Writest text to a file.
     *
     * @param filePath Absolute file path
     * @param text Text to write
     * @throws Exception
     */
    public static void WriteTextToFile(String filePath, String text) throws Exception
    {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
        out.write(text);
        out.close();
    }

    /** Writes a new line to a file (including a new line character).
     *
     * @param filePath Absolute file path
     * @param text Text to write
     * @throws Exception
     */
    public static void WriteLineToFile(String filePath, String text) throws Exception
    {
        WriteTextToFile(filePath, text + "\n");
    }

    /** Writes new lines to a file (including new line characters).
     *
     * @param filePath Absolute file path
     * @param lines List of lines to write
     * @throws Exception
     */
    public static void WriteLinesToFile(String filePath, ArrayList<String> lines) throws Exception
    {
        WriteTextToFile(filePath, Lists.Join(lines, "\n"));
    }

    /** Reads a single value from a file.
     *
     * @param filePath Absolute file path
     * @return Value
     * @throws Exception
     */
    public static String ReadScalarFromFile(String filePath) throws Exception
    {
        ArrayList<ArrayList<String>> fileRows = ParseDelimitedFile(filePath);
        if (fileRows.size() == 0)
            return "";
        if (fileRows.get(0).size() == 0)
            return "";
        return fileRows.get(0).get(0);
    }

    /** Reads lines from a file.
     *
     * @param filePath Absolute file path
     * @return Each line in the file
     * @throws Exception
     */
    public static ArrayList<String> ReadLinesFromFile(String filePath) throws Exception
    {
        return ReadLinesFromFile(filePath, null);
    }

    /** Reads lines from a file.
     *
     * @param filePath Absolute file path
     * @param commentChar Comment character (lines starting with this character are ignored)
     * @return Each line in the file
     * @throws Exception
     */
    public static ArrayList<String> ReadLinesFromFile(String filePath, String commentChar) throws Exception
    {
        ArrayList<String> rows = new ArrayList<String>();

        for (String line : new BigFileReader(filePath))
        {
            if (line.trim().length() == 0 || (commentChar != null && line.startsWith(commentChar)))
                continue;

            rows.add(line.trim());
        }

        return rows;
    }

    /** Reads all text from a file.
     *
     * @param file File object
     * @return String representation of text in a file
     * @throws Exception
     */
    public static String ReadTextFile(File file) throws Exception
    {
        return ReadTextFile(file.getAbsolutePath());
    }

    /** Reads all text from a file.
     *
     * @param filePath Absolute file path
     * @return String representation of text in a file
     * @throws Exception
     */
    public static String ReadTextFile(String filePath) throws Exception
    {
        StringBuilder text = new StringBuilder();

        for (String line : new BigFileReader(filePath))
            text.append(line + "\n");

        return text.toString();
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath) throws Exception
    {
        return ParseDelimitedFile(filePath, "\t");
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter) throws Exception
    {
        return ParseDelimitedFile(filePath, delimiter, "#");
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @param commentChar Comment character (lines starting with this character will be ignored)
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter, String commentChar) throws Exception
    {
        return ParseDelimitedFile(filePath, delimiter, commentChar, 0);
    }

    /** Parses a delimited file.
     *
     * @param filePath Absolute file path
     * @param delimiter Delimiter
     * @param commentChar Comment character (lines starting with this character will be ignored)
     * @param numLinesToSkip Number of lines to skip at the beginning of the file
     * @return List of lists containing each element in the file
     * @throws Exception
     */
    public static ArrayList<ArrayList<String>> ParseDelimitedFile(String filePath, String delimiter, String commentChar, int numLinesToSkip) throws Exception
    {
        if (!FileExists(filePath))
            throw new Exception("No file exists at " + filePath);

        //TODO: Use file iterator class in here

        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);

        int linesRead = 0;
        int linesSkipped = 0;
        String line;
        while ((line = br.readLine()) != null)
        {
            linesRead++;

            if (linesRead % 1000 == 0)
                Utilities.Log.Info(linesRead + " lines read from " + filePath);

            if (numLinesToSkip <= linesSkipped && !line.startsWith(commentChar))
            {
                boolean lastValueBlank = line.endsWith(delimiter);
                String[] lineValues = line.split(delimiter);

                if (lastValueBlank)
                    lineValues = Lists.AppendItemToArray(lineValues, "");

                rows.add(Lists.CreateStringList(lineValues));
            }

            linesSkipped++;
        }

        br.close();
        fr.close();

        return rows;
    }

    /** Copies a file from one location to another.
     *
     * @param sourceFilePath Source file path
     * @param destinationFilePath Destination file path
     * @throws Exception
     */
    public static void CopyFile(String sourceFilePath, String destinationFilePath) throws Exception
    {
        File sourceFile = new File(sourceFilePath);
        if (sourceFile.exists())
        {
            File destinationFile = new File(destinationFilePath);

            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destinationFile);

            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close();
            out.close();
        }
    }

    /** Deletes a file.
     *
     * @param file File object
     */
    public static void DeleteFile(File file)
    {
        if (file.exists())
            try
            {
                file.delete();
            }
            catch (Exception ex)
            {
                Utilities.Log.Debug("Could not delete " + file.getAbsolutePath() + "."); // Often this is not a problem, but we're recording it just in case.
            }
    }

    /** Deletes a file.
     *
     * @param filePath Absolute file path
     */
    public static void DeleteFile(String filePath)
    {
        DeleteFile(new File(filePath));
    }

    /** Deletes a file.
     *
     * @param dirPath Absolute file path
     */
    public static void DeleteDirectory(String dirPath)
    {
        File dir = new File(dirPath);

        if (dir.isDirectory())
            DeleteFile(dir);
    }

    /** Deletes all files in a directory.
     *
     * @param dir Absolute directory path
     */
    public static void DeleteFilesInDirectory(String dir)
    {
        DeleteFilesInDirectory(dir, "*");
        DeleteFilesInDirectory(dir, "*.*");
    }

    /** Deletes files in a directory that match a specified file pattern.
     *
     * @param dir Absolute directory path
     * @param pattern File pattern to match
     */
    public static void DeleteFilesInDirectory(String dir, String pattern)
    {
        for (File file : GetFilesInDirectory(dir, pattern))
            if (file.exists())
                file.delete();
    }

    /** Deletes files in a directory and its subdirectories that match a specified file pattern.
     *
     * @param directoryPath Absolute directory path
     * @param pattern File pattern to match
     * @throws Exception
     */
    public static void DeleteFilesRecursively(String directoryPath, String pattern) throws Exception
    {
        CreateDirectoryIfNotExists(directoryPath);

        File[] files = new File(directoryPath).listFiles();

        if (files == null || files.length == 0)
            return;

        for (File file : files)
        {
            //TODO: This is a temporary hack for matching file patterns. Use WildcardFilter?
            if (file.isFile())
            {
                if (!file.getAbsolutePath().contains(pattern))
                    continue;

                Utilities.Log.Debug("Deleting file from " + file.getAbsolutePath());
                file.delete();
            }
            else
                DeleteFilesRecursively(file.getAbsolutePath(), pattern);
        }
    }

    /** Deletes any empty files in a directory or its subdirectories.
     *
     * @param directoryPath Absolute directory path
     * @throws Exception
     */
    public static void DeleteEmptyDirectoriesRecursively(String directoryPath) throws Exception
    {
        CreateDirectoryIfNotExists(directoryPath);

        File[] files = new File(directoryPath).listFiles();

        if (files == null || files.length == 0)
            return;

        for (File file : files)
        {
            //TODO: This is a temporary hack for matching file patterns. Use WildcardFilter?
            if (!file.isFile())
            {
                if (file.listFiles().length == 0)
                {
                    Utilities.Log.Debug("Deleting empty directory " + file.getAbsolutePath());
                    file.delete();
                }
                else
                {
                    DeleteEmptyDirectoriesRecursively(file.getAbsolutePath());
                }
            }
        }
    }

    /** Deletes all files recursively that are in a specified directory. Then it deletes the empty directories recursively. Be careful with this one!
     *
     * @param directoryPath Absolute directory path
     * @throws Exception
     */
    public static void DeleteAllFilesAndDirectoriesRecursively(String directoryPath) throws Exception
    {
        DeleteFilesRecursively(directoryPath, "*");
        DeleteEmptyDirectoriesRecursively(directoryPath);
    }

    /** Gets array of files in a directory.
     *
     * @param dir Absolute directory path
     * @return Array of files
     */
    public static File[] GetFilesInDirectory(String dir)
    {
        return GetFilesInDirectory(dir, "*");
    }

    /** Gets array of files in a directory that match a specified file pattern.
     *
     * @param dir Absolute directory path
     * @param pattern File pattern to match
     * @return Array of files
     */
    public static File[] GetFilesInDirectory(String dir, String pattern)
    {
        File directory = new File(dir);
        File[] files = directory.listFiles(new WildCardFileFilter(pattern));

        if (files == null)
            return new File[0];

        return files;
    }

    /** Gets a list of file objects in a directory and its subdirectories that match a specified file pattern.
     *
     * @param dirPath Absolute directory path
     * @param pattern File pattern to match
     * @return List of matching files
     * @throws Exception
     */
    public static ArrayList<File> GetFilesInDirectoryRecursively(String dirPath, String pattern) throws Exception
    {
        return GetObjectsInDirectoryRecursively(dirPath, pattern, false);
    }

    /** Gets a list of file (and directory) objects in a directory and its subdirectories that match a specified file pattern.
     *
     * @param dirPath Absolute directory path
     * @param pattern File pattern to match
     * @param includeDirectories Whether to include directory objects also
     * @return List of matching files
     * @throws Exception
     */
    public static ArrayList<File> GetObjectsInDirectoryRecursively(String dirPath, String pattern, boolean includeDirectories) throws Exception
    {
        CreateDirectoryIfNotExists(dirPath);
        File directory = new File(dirPath);
        FilenameFilter filter = new WildCardFilenameFilter(pattern);
        ArrayList<File> results = new ArrayList<File>();

        for (File file : directory.listFiles())
        {
            if (filter.accept(directory, file.getName()))
                results.add(file);
            else
            {
                if (file.isDirectory())
                {
                    results.addAll(GetObjectsInDirectoryRecursively(file.getAbsolutePath(), pattern, includeDirectories));

                    if (includeDirectories && !results.contains(file))
                        results.add(file);
                }
            }
        }

        return results;
    }

    /** Checks whether a directory currently exists. If not, it (and any parent directories that don't exist) are attempted to be created.
     *
     * @param dirPath Absolute directory path
     * @return Absolute directory path
     * @throws Exception
     */
    public static String CreateDirectoryIfNotExists(String dirPath) throws Exception
    {
        File dir = new File(dirPath);
        if (!dir.exists())
        {
            if (!dir.mkdirs())
                throw new Exception("A new directory could not be created at " + dirPath + ".");
        }

        return dirPath;
    }

    /** Checks whether a file currently exists. If not, it will be attempted to be created.
     *
     * @param filePath Absolute file path
     * @return Absolute file path
     * @throws Exception
     */
    public static String CheckFileExists(String filePath) throws Exception
    {
        if (!FileExists(filePath))
            throw new Exception("No file exists at " + filePath + ".");

        return filePath;
    }

    /** Indicates whether a file exists (and is not a directory.
     *
     * @param filePath Absolute file path
     * @return Whether the file exists (and is not a directory)
     * @throws Exception
     */
    public static boolean FileExists(String filePath) throws Exception
    {
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }

    /** Checks whether the directory in an absolute file path exists. If not, it is created.
     *
     * @param filePath Absolute file path
     * @throws Exception
     */
    public static void CheckFileDirectoryExists(String filePath) throws Exception
    {
        if (!filePath.contains("/") && !filePath.contains("\\"))
            return;

        File file = new File(filePath);

        int indexLastSlash1 = file.getAbsolutePath().lastIndexOf("/");
        int indexLastSlash2 = file.getAbsolutePath().lastIndexOf("\\");

        int indexLastSlash = indexLastSlash1;
        if (indexLastSlash2 > indexLastSlash)
            indexLastSlash = indexLastSlash2;

        if (indexLastSlash > -1)
        {
            File dir = new File(file.getAbsolutePath().substring(0, indexLastSlash));
            if (dir.exists())
                return;

            if (!new File(file.getAbsolutePath().substring(0, indexLastSlash)).mkdirs())
                throw new Exception("A directory could not be created for " + filePath + ".");
        }
    }

    /** Moves a file from one location to another
     *
     * @param fromFilePath Absolute file path of existing file
     * @param toFilePath Absolute file path to be created
     */
    public static void MoveFile(String fromFilePath, String toFilePath)
    {
        File fromFile = new File(fromFilePath);
        File toFile = new File(toFilePath);

        if (fromFile.exists())
            fromFile.renameTo(toFile);
    }

    /** Creates a directory. If an error occurs while creating the directory, an exception is not thrown. It is understood that sometimes directories are attempted to be created when another thread is also trying to create it, and this sometimes is not considered problematic.
     *
     * @param directoryPath Absolute directory path
     */
    public static boolean CreateDirectoryNoFatalError(String directoryPath)
    {
        File dir = new File(directoryPath);
        if (!dir.exists())
        {
            try
            {
                dir.mkdirs();
                return true;
            }
            catch (Exception ex)
            {
                Utilities.Log.Info(ex);
                return false;
            }
        }

        return  true;
    }

    /** Deletes a directory.
     *
     * @param directoryPath Absolute directory path
     */
    public static void RemoveDirectory(String directoryPath)
    {
        File directory = new File(directoryPath);
        if (!directory.exists())
            return;

        DeleteFilesInDirectory(directoryPath);
        directory.delete();
    }

    /** Creates (atomically) an empty file.
     *
     * @param filePath Absolute file path
     * @return Whether the file was created successfully
     * @throws Exception
     */
    public static boolean CreateEmptyFile(String filePath) throws Exception
    {
        File file = new File(filePath);
        return CreateEmptyFile(file.getParent(), file.getName());
    }

    /** Creates (atomically) an empty file.
     *
     * @param directoryPath Absolute directory path
     * @param fileName File name
     * @return Whether the file was created successfully
     * @throws Exception
     */
    public static boolean CreateEmptyFile(String directoryPath, String fileName) throws Exception
    {
        if (!Files.CreateDirectoryNoFatalError(directoryPath))
            return false;

        if (!directoryPath.endsWith("/"))
            directoryPath += "/";

        try
        {
            return new File(directoryPath + fileName).createNewFile();
        }
        catch (Exception ex)
        {
            Utilities.Log.Info("File could not be created at " + directoryPath + fileName + ". This will be attempted again later.");
            Utilities.Log.Info(ex);

            return false;
        }
    }
}
