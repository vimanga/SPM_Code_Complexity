/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecomplexity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CodeComplexity {

    private int Cs = 0;
    private int Ctc = 0;
    private int Cnc = 0;
    private int TW = 0;
    private int Cps = 0;
    private int Cr = 0;
    private int Ci = 0;
    private int Cp = 0;
    private int braces = 1;              //will be incremented for each opening brace and decremented for each closing brace
    private boolean isComment = false;   //will be true if we are inside a multi-line comment
    public boolean isJava = false;      //will be true if code sample is in java
    private boolean nestedBlock = false;     //Used to identify nested blocks inside loops or 'if' statements
    private boolean isDoWhileLoop = false;   //Used to skip the next 'while' keyword after 'do' detected
    private int noSwitch = 0;
    private int noTry = 0;
    private HashMap<String, ArrayList<Integer>> results = new HashMap<>();
    private ArrayList<String> inheritance = new ArrayList<>();
    private ArrayList<String> methodNames = new ArrayList<>();

    /*HashSet used to identify if a word is a keyword in */
    private HashSet<String> keyWordSet = new HashSet<String>();

    public CodeComplexity() {
    }

    public CodeComplexity(String path, String type) {
        try {
            if (type.equalsIgnoreCase("java")) {
                isJava = true;
                getEveryFile(path, "java");
            } else {
                getEveryFile(path, "cpp");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Identifying every file in a location by extension
     *
     * @param path The folder which the method will scan recursively
     * @param extension The extension of the files to be scanned
     * @throws IOException
     */
    private void getEveryFile(String path, String extension) throws IOException {
        final String exten = extension.toLowerCase();

        Files.walk(Paths.get(path))
                .filter(p -> p.getFileName().toString().endsWith("." + exten))
                .forEach(this::forEveryFile);
    }

    /**
     * Identifying every line of the given file
     *
     * @param path path to the file
     */
    private void forEveryFile(Path path) {
        Cs = 0;
        try (Stream<String> stream = Files.lines(Paths.get(path.toString()))) {
            stream.forEach(this::forEveryLine);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ArrayList<Integer> values = new ArrayList<>();
        values.add(Cs);
        values.add(Ctc);
        values.add(Cnc);
        values.add(Ci);
        values.add(TW);
        values.add(Cps);
        values.add(Cr);

        results.put(path.getFileName().toString(), values);
    }

    /**
     * Used to execute methods on the string passed
     *
     * @param line the code line on which the methods will be executed
     */
    private void forEveryLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }
        if (isComment) {
            if (!line.contains("*/")) {
                return;
            } else {
                //multiline comment is over so reset isComment
                isComment = false;
                int end = line.indexOf("*/");
                line = line.substring(end, line.length());
            }
        }

        // Excluding import Statements
        if (line.contains("import")) {
            return;
        }

        // Excluding comments
        if (line.contains("//")) {
            int begin = line.indexOf("//");
            if (begin == 0) {
                return;
            }
            line = line.substring(0, begin);
        }
        if (line.contains("/*")) {
            int begin = line.indexOf("/*");
            int end = line.indexOf("*/");
            if (end == -1) {
                //isComment true means the multiline comment is not over.
                isComment = true;
                return;
            }
            line = line.substring(0, begin) + line.substring(end, line.length());

        }
        if (line.contains("\"")) {
            // Checking strings and then removing them.
            Cs += checkStrings(line);
            while(line.contains("\""))
            {
                int begin = line.indexOf("\"");
                String firstpart = line.substring(0, begin);
                line = line.substring(begin+1, line.length());
                int end = line.indexOf("\"");
                String secondpart = line.substring(end+1, line.length());
                line = firstpart + secondpart;
            }
        }

        //Calling All calculation methods
        
        Cs += miscellaneousOperators(line);
        Cs += logicalOperators(line);
        Cs += assignmentOperators(line); 
        Cs += arithmeticOperators(line);
        Cs += relationalOperators(line);
        Cs += manipulators(line);
        Cs += bitwiseOperators(line);
        Cs += keywords(line);
        Cs += identifiers(line);
        Cs += numbers(line); 
        
        Ctc += conditionalControlStructure(line);
        Ctc += iterativeControlStructure(line);
        Ctc += switchControlStructure(line);
        Ctc += trycatchStructure(line);
        
        Cnc += nestingControlStructure(line);
        
        Ci += inheritance(line);
        
        
        
        
        doCalculations(line);
    }

//****************************************************************************************************************************
//--------------------------------------------------- Identifing Operators ---------------------------------------------------
//****************************************************************************************************************************
    
    /**
     * Identifies Miscellaneous operators in line of Code.
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int miscellaneousOperators(String line) {
        int total = 0;
        if (!isJava) //Is C++
        {
            // Multiply by 2 since each operator is awarded 2 marks
            //Detecting &
            total = total + ((line.length() - line.replaceAll("(?<!&)&(?![&=])", "").length())) * 2;
            //Detecting *
            total = total + ((line.length() - line.replaceAll("\\*(?!=)", "").length())) * 2;
        }

        //Detecting .
        total = total + ((line.length() - line.replaceAll("\\.", "").length()));
        //Detecting ,
        total = total + ((line.length() - line.replaceAll(",", "").length()));
        //Detecting ->
        total = total + ((line.length() - line.replaceAll("->", "").length()) / 2);
        //Detecting ::
        total = total + ((line.length() - line.replaceAll("::", "").length()) / 2);

        return total;
    }

    /**
     * Identifies Logical operators in line of Code.
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int logicalOperators(String line) {
        int total = 0;

        //Detecting &&
        total = total + ((line.length() - line.replaceAll("&&", "").length()) / 2);
        //Detecting ||
        total = total + ((line.length() - line.replaceAll("\\|\\|", "").length()) / 2);
        //Detecting !
        total = total + ((line.length() - line.replaceAll("!(?!=)", "").length()));

        return total;
    }

    /**
     * Identifies Assignment operators in line of Code.
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int assignmentOperators(String line) {
        int total = 0;

        //Detecting +=
        total = total + ((line.length() - line.replaceAll("\\+=", "").length()) / 2);
        //Detecting -=
        total = total + ((line.length() - line.replaceAll("\\-=", "").length()) / 2);
        //Detecting *=
        total = total + ((line.length() - line.replaceAll("\\*=", "").length()) / 2);
        //Detecting /= 
        total = total + ((line.length() - line.replaceAll("\\/=", "").length()) / 2);
        //Detecting = 
        total = total + ((line.length() - line.replaceAll("(?<![=\\+\\-\\*/!><%&^|])=(?![&=])", "").length()));
        //Detecting >>>=
        total = total + ((line.length() - line.replaceAll(">>>=", "").length()) / 4);
        //Detecting |=
        total = total + ((line.length() - line.replaceAll("\\|=", "").length()) / 2);
        //Detecting &=
        total = total + ((line.length() - line.replaceAll("&=", "").length()) / 2);
        //Detecting %=
        total = total + ((line.length() - line.replaceAll("%=", "").length()) / 2);
        //Detecting <<=
        total = total + ((line.length() - line.replaceAll("<<=", "").length()) / 2);
        //Detecting >>=
        total = total + ((line.length() - line.replaceAll("(?<!>)>>=", "").length()) / 2);
        //Detecting ^=
        total = total + ((line.length() - line.replaceAll("\\^=", "").length()) / 2);

        return total;
    }

    /**
     * Identifies manipulators in line of Code.
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int manipulators(String line) {
        int total = 0;
        line = line.replaceAll(" ", "");
        if (!isJava) //Is C++
        {
            //Detecting 'cout<<'
            total = total + ((line.length() - line.replaceAll("cout<<", "").length()) / 6);
            //Detecting 'cin>>'
            total = total + ((line.length() - line.replaceAll("cin>>", "").length()) / 5);
        }
        return total;
    }

    /**
     * Identify arithmetic operators in a line of code
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int arithmeticOperators(String line) {
        int total = 0;

        //Detect +
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[+](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect -
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[-](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect *
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[*](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect /
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[/](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect %
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[%](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect ++
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])\\++(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect --
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])\\--(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);

        return total;
    }

    /**
     * identify relational operators in a line of code
     *
     * @param line The line to check
     * @return The number of points for Cs
     */
    protected int relationalOperators(String line) {
        int total = 0;

        //Detect ==
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])==(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect !=
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])!=(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect >
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])>(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect <
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])<(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect >=
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])>=(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect <=
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])<=(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);

        return total;
    }

    protected int bitwiseOperators(String line) {

        int total = 0;

        //Detect |
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[|](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect ^ Does not work needs to be fixed
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])\\^(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect ~
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])[~](?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
        //Detect <<
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])<<(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect >>
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])>>(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 2);
        //Detect <<<
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])<<<(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 3);
        //Detect >>>
        total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])>>>(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()) / 3);

        return total;
    }

    protected int keywords(String line) {

        int total = 0;

        //List of Keywords
        if (isJava) {
            ArrayList<String> jkeyList = new ArrayList<>();
            Collections.addAll(jkeyList, "assert", "boolean", "break", "byte", "case", "catch",
                    "char", "class", "continue", "default", "do", "double", "enum", "extends", "final", "finally",
                    "float", "for", "if", "implements", "import", "instanceof", " int", "interface", "long", "native",
                    "null", "package", "private", "protected", "short", "strictfp", "super", "switch",
                    "synchronized", "this", "transient", "void", "volatile", "while");

            Iterator<String> itr = jkeyList.iterator();

            while (itr.hasNext()) {
                String kw = itr.next();
                total = total + ((line.length() - line.replaceAll(kw, "").length()) / kw.length());
            }
        } else {
            ArrayList<String> ckeyList = new ArrayList<>();
            Collections.addAll(ckeyList, "break", "long",
                    "switch", "case", "enum", "register", "typedef", "char", "extern",
                    "return", "union", "const", "float", "short", "unsigned", "continue",
                    "for", "signed", "void", "default", "goto", "sizeof", "volatile", "do",
                    "if", "while", "asm", "dynamic_cast", "namespace",
                    "reinterpret_cast", "bool", "explicit", "static_cast",
                    "catch", "false", "operator", "template", "class", "friend",
                    "private", "this", "const", "cast", "inline",
                    "delete", "mutable", "protected", "true", "typeid", "typename",
                    "using", "virtual", "wchar_t");

            Iterator<String> itr = ckeyList.iterator();

            while (itr.hasNext()) {
                String kw = itr.next();
                total = total + ((line.length() - line.replaceAll(kw, "").length()) / kw.length());
            }
        }

        ArrayList<String> spkeyList = new ArrayList<>();
        Collections.addAll(spkeyList, "new", "delete", "throw ", "throws" );

        Iterator<String> itr = spkeyList.iterator();

        while (itr.hasNext()) {
            String spkw = itr.next();
            total = total + (((line.length() - line.replaceAll(spkw, "").length()) / spkw.length()) * 2);
        }

        return total;
    }

    protected int checkStrings(String line) {

        int total = 0;

        total = total + ((line.length() - line.replaceAll("\".*?", "").length()) / 2);

        return total;
    }

    public int numbers(String line) {
        int programType = (isJava) ? 0 : 2;//set program type 0 for java and, 2 for C++.
        int numberCount = 0;
        String character = " ";
        // add a non variable value in-case first character is a number
        line = "#" + line;
        /*Get character value and remove it form line String*/
        character = String.valueOf(line.charAt(0));
        line = line.substring(1);

        //Loop to access iterate through line ends when line is empty.
        while (!line.isEmpty()) {
            //is character NOT the start of a identifier.
            if (!Pattern.matches(CodeSizeConstrants.VARIABLE_CHAR[programType], character)) {
                /*Get character value and remove it form line String*/
                character = String.valueOf(line.charAt(0));
                line = line.substring(1);
                //is character a number.
                if (Pattern.matches("[0-9]", character)) {
                    //loop to find end of number.
                    while (Pattern.matches("[0-9.]", character)) {

                        //end loop if line is empty
                        if (line.isEmpty()) {
                            break;
                        }
                        /*Get character value and remove it form line String*/
                        character = String.valueOf(line.charAt(0));
                        line = line.substring(1);
                    }
                    numberCount++;

                    //end loop if line is empty
                    if (line.isEmpty()) {
                        break;
                    }

                }
            } else {
                /*Get character value and remove it form line String*/
                character = String.valueOf(line.charAt(0));
                line = line.substring(1);
            }

        }
        int count = numberCount;
        return count;
    }

    public int identifiers(String line) {
        int programType = (isJava) ? 0 : 2;//set program type 0 for java and, 2 for C++.
        String character = " "; 	//holds Temporary character value used with regex statement.
        int namesCount = 0;  		//counter increased when an identifiers.
        String word = ""; 			//word String hold an word value used to identify keywords.
        initKeyWordSet(programType); //fill keyWordSet with keyword of given program type. 
        //Loop to access iterate through line ends when line is empty.
        while (!line.isEmpty()) {

            try
            {
                /*Get character value and remove it form line String*/
                character = String.valueOf(line.charAt(0));
                line = line.substring(1);
                /*is character the start of a identifier*/
                if (Pattern.matches(CodeSizeConstrants.VARIABLE_START_WITH[programType], character)) {

                    word = word + character;//add character to word

                    /*Get character value and remove it form line String*/
                    character = String.valueOf(line.charAt(0));
                    line = line.substring(1);

                    /*loop that check if the next character is part of 
                                    * the identifier and then adds to word */
                    do{

                        word = word + character;//add character to word
                        /*Get character value and remove it form line String*/
                        if(line.isEmpty()) {
                        	break;
                        }
                        character = String.valueOf(line.charAt(0));
                        line = line.substring(1);
                    }while (Pattern.matches(CodeSizeConstrants.VARIABLE_CHAR[programType], character));

                    //If word is keyword reset word.
                    if (keyWordSet.contains(word)) {
                        word = "";//Reset word value to empty. 
                    } else {
                        /*If word is not a keyword 
                                            reset word and increase namesCount.*/
                        namesCount++;
                        word = "";
                    }
                }
            }catch(StringIndexOutOfBoundsException ignored)
            {}
        }
        return namesCount;
    }
    
    
//****************************************************************************************************************************
//--------------------------------------------------------- Sprint 2 ---------------------------------------------------------
//**************************************************************************************************************************** 
    
    /**
     * identify and grade conditional control structures the logical/bitwise operators within them
     * @param line The code line to grade
     * @return The obtained grade
     */
    public int conditionalControlStructure(String line)
    {
        int total = 0;
        
        total = total + ((line.length() - line.replaceAll("(?<!\\w)if(?!\\w)", "").length()) / 2);
        if(total > 0)  // 'if' detected
        {            
            //Detecting &&
            total = total + ((line.length() - line.replaceAll("&&", "").length()) / 2);
            //Detecting &
            total = total + ((line.length() - line.replaceAll("(?<!&)&(?![&=])", "").length()));
            //Detecting ||
            total = total + ((line.length() - line.replaceAll("\\|\\|", "").length()) / 2);
            //Detecting |
            total = total + ((line.length() - line.replaceAll("(?<!\\|)\\|(?!\\|)", "").length()));
        }      
        return total;
    }
    
    /**
     * identify and grade nesting of control structures based on the level of nesting.
     * @param line The code line to grade
     * @return The obtained grade
     */
    public int nestingControlStructure(String line)
    {
        int total = 0;
        // Detecting for,while or do-while loops
        total = total + ((line.length() - line.replaceAll("\\bfor\\b", "").length()) / 3)*braces;
        total = total + ((line.length() - line.replaceAll("\\bwhile\\b", "").length()) / 5)*braces;
        total = total + ((line.length() - line.replaceAll("\\bdo\\b", "").length()) / 2)*braces;  //Not needed becaues of 'while' at end?

        if(total > 0)   //one of the above keywords have been detected
        {
            if(line.matches(".*\\bwhile\\b.*") && isDoWhileLoop)    //skipping the 'while' after 'do'
            {
                return 0;
            }
            if(line.matches(".*\\bdo\\b.*"))    //if 'do' keyword detected
            {
                isDoWhileLoop = true;
            }
        }
        
        //Detecting 'if' statement
        total = total + ((line.length() - line.replaceAll("\\bif\\b", "").length()) / 2)*braces;
        
        if(total >0 || nestedBlock)
        {
            nestedBlock = true;
            if(line.contains("{"))
            {
                braces++;
            }
            if(line.contains("}"))
            {
                braces--;
            }
        }
        else if(braces == 1)
        {
            nestedBlock = false;
        }
        
        return total;
    }
    
    public int iterativeControlStructure(String line)
    {
        int total = 0;
        
        total = total + (((line.length() - line.replaceAll("(?<!\\w)for(?!\\w)", "").length()) / 3)*2);
        total = total + (((line.length() - line.replaceAll("(?<!\\w)while(?!\\w)", "").length()) / 5)*2 );
        //dont need add a seperate method to calculate do-while
        
        if(total > 0)  //a loop detected detected
        {            
            //Detecting &&
            total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])&&(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length()));
            //Detecting ||
            total = total + ((line.length() - line.replaceAll("\\|\\|", "").length()));
            //Detecting &
            total = total + ((line.length() - line.replaceAll("(?<![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])&(?![\\=\\<\\>\\!\\+\\-\\?\\|\\@\\#\\$\\%\\^\\&\\*\\/])", "").length())*2);
            //Detecting |
            total = total + ((line.length() - line.replaceAll("(?<!\\|)\\|(?!\\|)", "").length())*2);
        }      
        return total;
    }
    

    public int switchControlStructure(String line)
    {
        int total = 0;
        
        noSwitch += (((line.length() - line.replaceAll("(?<!\\w)switch(?!\\w)", "").length()) / 6));
        
        
        if(noSwitch > 0)  //switch detected
        {            
            //Detecting cases
            total = total + ((line.length() - line.replaceAll("(?<!\\w)case(?!\\w)", "").length()) / 4);
        }      
        return total;
    }
    
 
    public int trycatchStructure(String line){
        
        int total = 0;
        
        noTry += (((line.length() - line.replaceAll("(?<!\\w)try(?!\\w)", "").length()) / 3));
        
        
        if(noTry > 0)  //try detected
        {            
            //Detecting catch
            total = total + ((line.length() - line.replaceAll("(?<!\\w)catch(?!\\w)", "").length()) / 5);
        }      
        return total;
    }
    
    /**
     * Grading inheritance within the code
     * @param line The code line to grade
     * @return The obtained grade
     */
    public int inheritance(String line)
    {
        int total = 0;
        
        //Java
        if(isJava)
        {
            if(line.contains("extends"))
            {
                int i = 0;
                while(!line.split(" ")[i].equalsIgnoreCase("extends"))
                {
                    i++;
                }

                if(!inheritance.contains(line.split(" ")[i+1]))
                {
                    inheritance.add(line.split(" ")[i+1]);  //word after 'extends' keyword
                }
                else    //If not a chain inheritance
                {
                    if(inheritance.indexOf(line.split(" ")[i+1]) == 0)
                    {
                        inheritance.remove(1);
                    }
                }
                inheritance.add(line.split(" ")[i-1]);  //word before 'extends' keyword           

                total = total + (inheritance.indexOf(line.split(" ")[i-1])+1);
            }
        }
        //C++
        else
        {
            if(line.matches(".*\\bclass\\b(?=.*:).*")) //Contains character ':' somewhere after keyword 'class'
            {
                int i = 0;
                while(!line.split(" ")[i].equalsIgnoreCase(":"))
                {
                    i++;
                }
                int y = i;               
                
                if(line.split(" ")[i+1].matches(".*\\b(public|private|protected)\\b.*"))    //If word after ':' is private/public/protected, then skip it
                {
                    i++;
                }

                if(!inheritance.contains(line.split(" ")[i+1]))
                {
                    inheritance.add(line.split(" ")[i+1]);  //word after ':'
                }
                else    //If not a chain inheritance
                {
                    if(inheritance.indexOf(line.split(" ")[i+1]) == 0)
                    {
                        inheritance.remove(1);
                    }
                }
                
                inheritance.add(line.split(" ")[y-1]);  //word before ':'    
                

                total = total + (inheritance.indexOf(line.split(" ")[y-1])+1);
            }
        }
        
        return total;
    }
    
    /**
     * Grading Recursive calls within the code
     * @param line The code line to grade
     * @return The obtained grade
     */
    public boolean recursive(String line)
    {
        if(line.matches(".*\\b(public|private|protected)\\b(?!.*\\bmain\\b)(?=.*[\\(])(?=.*[\\)]).*"))  //Checks for given keywords, NOT conataining the word 'main' AND followed by a '(' AND a ')'
        {
            line = line.substring(0, line.indexOf("("));
            
            int lastIndex = line.split(" ").length-1;
            String method = line.split(" ")[lastIndex]; //Name of the method
            
            if(!methodNames.contains(method))
            {
                methodNames.add(method);  //adding method name to array
            }
        }
        else
        {
            for(String method : methodNames)
            {
                if(line.matches(".*\\b"+method+"\\b.*"))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
    
    /**
     * Calculating Ci, TW, Cps, Cr and Cp values
     */
    public void doCalculations(String line)
    {
        TW = Ctc + Cnc + Ci; 
        Cps = Cs * TW;
        if(recursive(line))
        {
            Cr = Cps*2;
            Cp = Cps + Cr;
        }
        else
        {
            Cp = Cps;
        }
    }
    
    
    

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*
        Regex Expressions Explained:
            !(?!=)                              '!' not followed by '='
            (?<!&)&(?![&=])                     '&' not preceded by a '&' or followed by characters { &= }
            \*(?!=)                             '*' not followed by a '='
            (?<![=\+\-\!/*><%&^|])=(?![&=])     '=' not preceded by characters { =\+\-\*!/><%&^|' } or followed by '='
            (?<!>)>>=                           '>>=' not preceded by '>'
            \\bfor\\b                           '\b' are work boundaries to ensure that strings are matched as complete words and not substrings of other words
     */
    /**
     * Used for directly passing code instead of directories
     *
     * @param line The line of code to grade
     */
    public void codeOnly(String line) {
        forEveryLine(line);
    }

    public int getCs() {
        return Cs;
    }
    
    public int getCtc() {
        return Ctc;
    }
    
    public int getCnc()
    {
        return Cnc;
    }
    
    public int getTW()
    {
        return TW;
    }

    public int getCps()
    {
        return Cps;
    }

    public int getCr()
    {
        return Cr;
    }

    public int getCi()
    {
        return Ci;
    }

    public void resetAllGrades() {
        Cs = 0;
        Ctc = 0;
        Cnc = 0;
        TW = 0;
        Cps = 0;
        Cr = 0;
        Ci = 0;
        Cp = 0;
    }

    public HashMap<String, ArrayList<Integer>> getResults() {
        return results;
    }

    /*this method initializes the keyWordSet HashSet with values 
	 * of keywordList array*/
    private void initKeyWordSet(int programType) {
        for (String key : CodeSizeConstrants.KEY_WORD_LIST[programType]) {
            keyWordSet.add(key);
        }
    }

}
