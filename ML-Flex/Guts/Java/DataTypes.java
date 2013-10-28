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

/** This class contains utility methods for determining whether String values can be converted to other data types. It also contains methods for performing those conversions.
 * @author Stephen Piccolo
 */
public class DataTypes
{
    /** Indicates whether a list of String objects only has binary values (0 or 1). Missing values are ignored.
     *
     * @param values List of String objects
     * @return Whether the list only has binary values
     */
    public static boolean HasOnlyBinary(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsBinary(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a String value is binary (contains either a 0 or 1).
     *
     * @param value Value to be tested
     * @return Whether the value is binary
     */
    public static boolean IsBinary(String value)
    {
        return value.equals("0") || value.equals("1");
    }

    /** Indicates whether a list of String objects contains only numeric values. Missing values are ignored.
     *
     * @param values List of values
     * @return Whether the list only contains numeric values
     */
    public static boolean HasOnlyNumeric(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsDouble(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a list of String objects contains only integer values. Missing values are ignored.
     *
     * @param values List of values
     * @return Whether the list only contains integer values
     */
    public static boolean HasOnlyIntegers(ArrayList<String> values)
    {
        for (String value : values)
        {
            if (value.equals(Settings.MISSING_VALUE_STRING))
                continue;

            if (!IsInteger(value))
                return false;
        }

        return true;
    }

    /** Indicates whether a String value contains either "true" or "false."
     *
     * @param value Value to be tested.
     * @return Whether the value contains either "true" or "false."
     */
    public static boolean IsBoolean(String value)
    {
        return value.toLowerCase().equals("true") || value.toLowerCase().equals("false");
    }

    /** Indicates whether a String value can be converted to a double value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted to a double
     */
    public static boolean IsDouble(String value)
    {
        try
        {
            Double.parseDouble(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /** Indicates whether a String value can be converted to an integer value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted to an integer
     */
    public static boolean IsInteger(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

     /** Indicates whether a String value can be converted to either a double value or an integer value.
     *
     * @param value Value to be tested
     * @return Whether the value can be converted either to a double value or an integer value
     */
    public static boolean IsNumeric(String value)
    {
        return IsDouble(value) || IsInteger(value);
    }

    /** For a set of values, this gets the "general" data type of the values.
     *
     * @param values Values to be tested
     * @return General data type of the values
     */
    public static GeneralDataType GetGeneralDataType(ArrayList<String> values)
    {
        if (HasOnlyBinary(values))
            return GeneralDataType.Binary;

        if (HasOnlyNumeric(values))
            return GeneralDataType.Continuous;

        return GeneralDataType.Nominal;
    }
}
