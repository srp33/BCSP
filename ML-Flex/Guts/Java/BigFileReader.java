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

import java.util.*;
import java.io.*;

/** This utility class is designed to aid in the process of parsing (potentially large) text files. It stores little data in memory, thus making it possible to parse very large files.
 * @author Stephen Piccolo
 */
public class BigFileReader implements Iterable<String>
{
    private BufferedReader _reader;

    /** Constructor
     *
     * @param file File indicating location of file to be read
     * @throws Exception
     */
    public BigFileReader(File file) throws Exception
    {
        this(file.getAbsolutePath());
    }

    /** Constructor
     *
     * @param filePath Absolute file path of file to be read
     * @throws Exception
     */
    public BigFileReader(String filePath) throws Exception
    {
        _reader = new BufferedReader(new FileReader(filePath));
    }

    private void Close()
    {
        try
        {
            _reader.close();
        }
        catch (Exception ex)
        {
            Utilities.Log.ExceptionFatal(ex);
        }
    }

    public Iterator<String> iterator()
    {
        return new BigFileIterator();
    }

    /** Reads a single line of the file
     *
     * @return Text of line
     * @throws Exception
     */
    public String ReadLine() throws Exception
    {
        return _reader.readLine();
    }

    /** Reads multiple lines of the file.
     *
     * @param numLines Number of lines to read
     * @throws Exception
     */
    public void ReadLines(int numLines) throws Exception
    {
        for (int i=0; i<numLines; i++)
            ReadLine();
    }

    /** This class iterates over the lines of a (potentially big) file
     * @author Stephen Piccolo
     */
    public class BigFileIterator implements Iterator<String>
    {
        private String _currentLine;
        private int _count = 0;

        public boolean hasNext()
        {
            try
            {
                _currentLine = _reader.readLine();
            }
            catch (Exception ex)
            {
                _currentLine = null;
                Utilities.Log.ExceptionFatal(ex);
            }

            if (_currentLine == null)
            {
                try
                {
                    Close();
                }
                catch (Exception ex)
                {
                    Utilities.Log.ExceptionFatal(ex);
                }
                return false;
            }
            else
                return true;
        }

        public String next()
        {
            _count++;

            if (_count % 10000 == 0)
                Utilities.Log.Info("Lines read: " + _count);

            return _currentLine.trim();
        }

        public void remove()
        {
        }
    }
}
