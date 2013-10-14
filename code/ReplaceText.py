import os, sys, glob
import utilities

inFilePath = sys.argv[1]
findValue = sys.argv[2].decode('string-escape')
replaceValue = sys.argv[3].decode('string-escape')
outFilePath = sys.argv[4]

text = utilities.readTextFromFile(inFilePath)
text = text.replace(findValue, replaceValue)
utilities.writeScalarToFile(text, outFilePath)
