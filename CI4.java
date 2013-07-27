// Hand-written S4 compiler
import java.io.*;
import java.util.*;
//======================================================
class CI4
{  
    public static void main(String[] args) throws 
    IOException
    {
        System.out.println("CI4 compiler written by Kevin Dougherty");

        boolean debug = false;
        if (args.length >= 1)
            for (int i = 0; i < args.length - 1; i++)
                if (args[0].equalsIgnoreCase("-debug_token_manager"))  
                    debug = true;
                else
                {
                    System.err.println("Bad command line arg");
                    System.exit(1);
                }
                else
                {
                    System.err.println("No input file specified");
                    System.exit(1);
        }

        // build the input and output file names
        String inFileName = args[args.length - 1] + ".s";
        String outFileName = args[args.length - 1] + ".a";

        // construct file objects
        Scanner inFile = new Scanner(new File(inFileName));
        PrintWriter outFile = new PrintWriter(outFileName);

        // identify compiler/author in the output file
        outFile.println("; from CI4 compiler written by Kevin Dougherty");

        // construct objects that make up compiler
        CI4SymTab st = new CI4SymTab();
        CI4TokenMgr tm =  new CI4TokenMgr(
                inFile, outFile, debug);
        CI4CodeGen cg = new CI4CodeGen(outFile, st);
        CI4Parser parser = new CI4Parser(st, tm, cg);

        // parse and translate
        try
        {
            parser.parse();
        }      
        catch (RuntimeException e) 
        {
            System.err.println(e.getMessage());
            outFile.println(e.getMessage());
            outFile.close();
            System.exit(1);
        }

        outFile.close();
    }
}                                           // end of CI4
//======================================================
interface CI4Constants
{
    // integers that identify token kinds
    int EOF = 0;
    int PRINTLN = 1;
    int UNSIGNED = 2;
    int ID = 3;
    int ASSIGN = 4;
    int SEMICOLON = 5;
    int LEFTPAREN = 6;
    int RIGHTPAREN = 7;
    int PLUS = 8;
    int MINUS = 9;
    int TIMES = 10;
    int ERROR = 11;
    int DIVIDE = 12;
    int LEFTBRACE = 13;
    int RIGHTBRACE = 14;
    int PRINT = 15;
    int READINT = 16;
    int STRING = 17;
    int WHILE = 18;
    int IF = 19;
    int ELSE = 20;
    int DO = 21;
    int PUSHCONSTANT = 22;
    int PUSH = 23;
    int HALT = 24;
    int DUPE = 25;
    int PRINTSTRING = 26;
    int NEG = 27;
    int NEWLINE = 28;
    int JA = 29;
    int JZ = 30;
    int JNZ = 31;

    // tokenImage provides string for each token kind
    String[] tokenImage = 
        {
            "<EOF>",
            "\"println\"",
            "<UNSIGNED>",
            "<ID>",
            "\"=\"",
            "\";\"",
            "\"(\"",
            "\")\"",
            "\"+\"",
            "\"-\"",
            "\"*\"", 
            "<ERROR>",
            "\"/\"",
            "\"{\"",
            "\"}\"",
            "\"print\"",
            "\"readint\"",
            "<STRING>",
            "while",
            "if",
            "else",
            "do"
        };
}                        // end of CI4Constants interface
class CI4SymTab
{
    private ArrayList<String> symbol;
    private ArrayList<Integer> value;
    //-----------------------------------------
    public CI4SymTab()
    {
        symbol = new ArrayList<String>();
        value  = new ArrayList<Integer>();
    }
    //-----------------------------------------
    public int enter(String s)
    {
        int index = symbol.indexOf(s);

        // if s is not in symbol, then add it 
        if (index < 0){
            symbol.add(s);
            value.add(0);
        }
        index = symbol.indexOf(s);
        return index;
    }
    //-----------------------------------------
    public String getSymbol(int index)
    {
        return symbol.get(index);
    }
    //-----------------------------------------
    public int getSize()
    {
        return symbol.size();
    }

    public Integer getValue(int index)
    {
        return value.get(index);
    }

    public void setValue(int index, Integer v)
    {
        value.set(index, v);//star with 0 when adding
    }
}                                     // end of I1SymTab
//======================================================
class CI4TokenMgr implements CI4Constants
{
    private Scanner inFile;          
    private PrintWriter outFile;
    private boolean debug;
    private char currentChar;
    private int currentColumnNumber;
    private int currentLineNumber;
    private String inputLine;     // holds 1 line of input
    private Token token;          // holds 1 token
    private StringBuffer buffer;  // token image built here
    private boolean inString;
    //-----------------------------------------
    public CI4TokenMgr(Scanner inFile, 
    PrintWriter outFile, boolean debug)
    {
        this.inFile = inFile;
        this.outFile = outFile;
        this.debug = debug;
        currentChar = '\n';        //  '\n' triggers read
        currentLineNumber = 0;
        buffer = new StringBuffer();
        inString = false;
    }
    //-----------------------------------------
    public Token getNextToken()
    {
        // skip whitespace
        while (Character.isWhitespace(currentChar))
            getNextChar();

        token = new Token();
        token.next = null;
        token.beginLine = currentLineNumber;
        token.beginColumn = currentColumnNumber;

        // check for EOF
        if (currentChar == EOF)
        {
            token.image = "<EOF>";
            token.endLine = currentLineNumber;
            token.endColumn = currentColumnNumber;
            token.kind = EOF;
        }

        else  // check for unsigned int
        if (Character.isDigit(currentChar)) 
        {
            buffer.setLength(0);  // clear buffer
            do  // build token image in buffer
            {
                buffer.append(currentChar);
                token.endLine = currentLineNumber;
                token.endColumn = currentColumnNumber;
                getNextChar();
            } while (Character.isDigit(currentChar));
            token.image = buffer.toString();
            token.kind = UNSIGNED;
        }

        else  // check for identifier
        if (Character.isLetter(currentChar)) 
        { 
            buffer.setLength(0);  // clear buffer
            do  // build token image in buffer
            {
                buffer.append(currentChar);
                token.endLine = currentLineNumber;
                token.endColumn = currentColumnNumber;
                getNextChar();
            } while (Character.isLetterOrDigit(currentChar));
            token.image = buffer.toString();

            // check if keyword
            if (token.image.equals("println"))
                token.kind = PRINTLN;
            else
            if (token.image.equals("print"))
                token.kind = PRINT;
            else
            if (token.image.equals("readint"))
                token.kind = READINT;
            else
            if (token.image.equals("while"))
                token.kind = WHILE;
            else
            if (token.image.equals("if"))
                token.kind = IF;
            else
            if (token.image.equals("else"))
                token.kind = ELSE;
            else
            if (token.image.equals("do"))
                token.kind = DO;
            else  // not a keyword so kind is ID
                token.kind = ID;
        }
        else
        if (currentChar == '"') 
        {
            int backslashCount = 0;
            inString = true;
            buffer.setLength(0);  // clear buffer
            getNextChar();
            do  // build token image in buffer
            {
                buffer.append(currentChar);
                if (currentChar == '\\'){
                    if (backslashCount == 0)
                        buffer.setLength(buffer.length()-1);
                    backslashCount = (backslashCount + 1) % 2;

                }
                else
                    backslashCount = 0;
                getNextChar();
                if(currentChar == 'n' && backslashCount == 1)
                {

                    //buffer.setLength(buffer.length()-1);
                    buffer.append('\n');
                    getNextChar();
                    if (currentChar == '"')
                        break;
                }
                if (currentChar == '\n' || currentChar == '\r')
                    if (backslashCount == 1)
                    {
                        //buffer.setLength(buffer.length() - 1);
                        backslashCount = 0;
                        getNextChar();
                    }
                    else  
                        break;
            } while (currentChar != '"' || backslashCount == 1);
            if (currentChar =='"')
            {
                //buffer.append(currentChar);
                token.kind = STRING;
            }
            else
                token.kind = ERROR;
            token.endLine = currentLineNumber;
            token.endColumn = currentColumnNumber;
            getNextChar();
            token.image = buffer.toString();
            inString = false;
        }

        else  // process single-character token
        {  
            switch(currentChar)
            {
                case '=':
                token.kind = ASSIGN;
                break;                               
                case ';':
                token.kind = SEMICOLON;
                break;                               
                case '(':
                token.kind = LEFTPAREN;
                break;                               
                case ')':
                token.kind = RIGHTPAREN;
                break;                               
                case '+':
                token.kind = PLUS;
                break;                               
                case '-':
                token.kind = MINUS;
                break;                               
                case '*':
                token.kind = TIMES;
                break;                               
                case '/':
                token.kind = DIVIDE;
                break;                               
                case '{':
                token.kind = LEFTBRACE;
                break;                               
                case '}':
                token.kind = RIGHTBRACE;
                break;                               
                default:
                token.kind = ERROR;
                break;                               
            }

            // save currentChar as String in token.image
            token.image = Character.toString(currentChar);

            // save token end location
            token.endLine = currentLineNumber;
            token.endColumn = currentColumnNumber;

            getNextChar();  // read beyond end
        }

        // token trace appears as comments in output file
        if (debug)
            outFile.printf(
                "; kd=%3d bL=%3d bC=%3d eL=%3d eC=%3d im=%s%n",
                token.kind, token.beginLine, token.beginColumn, 
                token.endLine, token.endColumn, token.image);

        return token;
    }     
    //-----------------------------------------
    private void getNextChar()
    {
        if (currentChar == EOF)
            return;

        if (currentChar == '\n')
        {
            if (inFile.hasNextLine())     // any lines left?
            {
                inputLine = inFile.nextLine();  // get next line
                // output source line as comment
                outFile.println("; " + inputLine);
                inputLine = inputLine + "\n";   // mark line end
                currentLineNumber++;
                currentColumnNumber = 0;
            }                                
            else  // at EOF
            {
                currentChar = EOF;
                return;
            }
        }

        // check if single-line comment
        if (!inString &&
        inputLine.charAt(currentColumnNumber) == '/' &&
        inputLine.charAt(currentColumnNumber+1) == '/')
            currentChar = '\n';  // forces end of line
        else
            currentChar = 
            inputLine.charAt(currentColumnNumber++);
    }
}                             // end of CI4TokenMgr class
//======================================================
class CI4Parser implements CI4Constants
{
    private CI4SymTab st;
    private CI4TokenMgr tm;
    private CI4CodeGen cg;
    private Token currentToken;
    private Token previousToken; 
    private Stack<Integer> s;

    //-----------------------------
    public CI4Parser(CI4SymTab st,CI4TokenMgr tm,CI4CodeGen cg)
    {
        this.st = st;
        this.tm = tm;
        this.cg = cg;
        s = new Stack<Integer>();
        // prime currentToken with first token
        currentToken = tm.getNextToken(); 
        previousToken = null;
    }
    //-----------------------------------------
    // Construct and return an exception that contains
    // a message consisting of the image of the current
    // token, its location, and the expected tokens.
    //
    private RuntimeException genEx(String errorMessage)
    {
        return new RuntimeException("Encountered \"" + 
            currentToken.image + "\" on line " + 
            currentToken.beginLine + " column " + 
            currentToken.beginColumn +
            System.getProperty("line.separator") + 
            errorMessage);
    }
    //-----------------------------------------
    // Advance currentToken to next token.
    //
    private void advance()
    {
        previousToken = currentToken; 

        // If next token is on token list, advance to it.
        if (currentToken.next!=null)
            currentToken = currentToken.next;

        // Otherwise, get next token from token mgr and 
        // put it on the list.
        else
            currentToken = 
            currentToken.next = tm.getNextToken();
    }
    //-----------------------------------------
    // getToken(i) returns ith token without advancing
    // in token stream.  getToken(0) returns 
    // previousToken.  getToken(1) returns currentToken.
    // getToken(2) returns next token, and so on.
    //
    private Token getToken(int i)
    {
        if (i <= 0)
            return previousToken;

        Token t = currentToken;
        for (int j = 1; j < i; j++)  // loop to ith token
        {
            // if next token is on token list, move t to it
            if (t.next != null)
                t = t.next;

            // Otherwise, get next token from token mgr and 
            // put it on the list.
            else
                t = t.next = tm.getNextToken();
        }
        return t;
    }
    //-----------------------------------------
    // If the kind of the current token matches the
    // expected kind, then consume advances to the next
    // token. Otherwise, it throws an exception.
    //
    private void consume(int expected)
    {
        if (currentToken.kind == expected){
            //System.out.println(currentToken.image);
            advance();}
        else
            throw genEx("Expecting " + tokenImage[expected]);
    }
    //-----------------------------------------
    public void parse()
    {
        //System.out.println("Start Program");
        program();
        //System.out.println("done parse");
        cg.makevtab(st.getSize());
        //System.out.println("made vtab");
        cg.interpret();
        //System.out.println("Done");
    }
    //-----------------------------------------
    private void program()
    {
        statementList();
        //cg.endCode();
        if (currentToken.kind != EOF)
            throw genEx("Expecting <EOF>");
    }
    //-----------------------------------------
    private void statementList()
    {
        if (currentToken.kind == EOF)
            return;
        statement();
        statementList();
    }
    //-----------------------------------------
    private void statement()
    { try
        {
            switch(currentToken.kind)
            {
                case ID: 
                assignmentStatement(); 
                break;
                case PRINTLN:    
                printlnStatement(); 
                break;
                case PRINT:
                printStatement();
                break;
                case SEMICOLON:
                nullStatement();
                break;
                case LEFTBRACE:
                compoundStatement();
                break;
                case READINT:
                readintStatement();
                break;
                case WHILE:
                whileStatement();
                break;
                case IF:
                ifStatement();
                break;
                case DO:
                doWhileStatement();
                break;
                default:         
                throw genEx("Expecting statement");
            }
        }
        catch(RuntimeException e)
        {
            System.err.println(e.getMessage());
            //cg.emitString(e.getMessage());

            if (currentToken.kind == LEFTBRACE)
                advance();  // move off bad token
            while (currentToken.kind != SEMICOLON &&
            currentToken.kind != RIGHTBRACE &&
            currentToken.kind != LEFTBRACE &&
            currentToken.kind != EOF)
                advance();
            if (currentToken.kind != LEFTBRACE && 
            currentToken.kind != EOF)
                advance();
        }
    }

    //-----------------------------------------
    private void assignmentStatement()
    {
        Token t;

        t = currentToken;
        consume(ID);
        int value = st.enter(t.image);
        consume(ASSIGN);
        assignmentTail();
        cg.emit(ASSIGN);
        cg.emit(value);
        consume(SEMICOLON);
    }
    //-----------------------------------------
    private void assignmentTail()
    {
        Token t;
        if (getToken(1).kind == ID &&
        getToken(2).kind == ASSIGN)
        {
            t = currentToken;
            consume(ID);
            int value = st.enter(t.image);
            //cg.emitInstruction("pc", t.image);

            consume(ASSIGN);
            assignmentTail();
            cg.emit(DUPE);
            cg.emit(ASSIGN);
            cg.emit(value);

        }
        else
            expr();
    }
    //-----------------------------------------
    private void printlnStatement()
    {
        consume(PRINTLN);
        consume(LEFTPAREN);
        if (currentToken.kind != RIGHTPAREN)
            printArg();
        cg.emit(NEWLINE);

        consume(RIGHTPAREN);
        consume(SEMICOLON);
    }
    //-----------------------------------------
    private void printArg()
    {     
        Token t;

        if (currentToken.kind != STRING)
        {
            expr();
            cg.emit(PRINT);
        }
        else
        {
            t = currentToken;
            consume(STRING);
            //Enter string method, return index of starting string, is second emit after printstring
            int stringLoc = cg.saveString(t.image);
            //System.out.println(t.image + " at " + stringLoc);

            cg.emit(PRINTSTRING);
            cg.emit(stringLoc);
        }
    }
    //-----------------------------------------
    private void printStatement()
    {
        consume(PRINT);
        consume(LEFTPAREN);
        printArg();
        consume(RIGHTPAREN);
        consume(SEMICOLON);
    }
    //-----------------------------------------
    private void nullStatement()
    {
        consume(SEMICOLON);
    }
    //-----------------------------------------
    private void compoundStatement()
    {
        consume(LEFTBRACE);
        compoundList();
        consume(RIGHTBRACE);
    } 
    //-----------------------------------------
    private void compoundList()
    {
        if (currentToken.kind == RIGHTBRACE)
            return;
        statement();
        compoundList();
    } 
    //-----------------------------------------
    private void readintStatement()
    {
        Token t;

        consume(READINT);
        consume(LEFTPAREN);
        t = currentToken;
        consume(ID);
        int value = st.enter(t.image);
        //cg.emit(PUSHCONSTANT);
        cg.emit(READINT);
        cg.emit(value);
        consume(RIGHTPAREN);
        consume(SEMICOLON);
    } 
    //-----------------------------------------
    private void whileStatement()
    {
        String label1, label2;
        //emite jz, get next adress, 
        int toBeFilledVal = 0;

        consume(WHILE);
        int start = cg.getSize();
        //label1 = cg.getLabel();
        //cg.emitLabel(label1);
        consume(LEFTPAREN);
        expr();
        consume(RIGHTPAREN);
        cg.emit(JZ);

        cg.emit(toBeFilledVal);
        int pc = cg.getSize();
        //label2 = cg.getLabel();
        //cg.emitInstruction("jz", label2);
        statement();
        cg.emit(JA);
        cg.emit(start);
        cg.changeCode(pc-1, cg.getSize());
        //cg.emitInstruction("ja", label1);
        //cg.emitLabel(label2);
    }
    //-----------------------------------------
    private void ifStatement()
    {
        int label1;
        int dumbyVal = 0;
        int pc;

        consume(IF);
        consume(LEFTPAREN);
        expr();
        cg.emit(JZ);
        cg.emit(dumbyVal);
        pc = cg.getSize()-1;
        consume(RIGHTPAREN);

        //label1 = cg.getLabel();
        //cg.emitInstruction("jz", label1);
        statement();
        elsePart(pc);
    }
    //-----------------------------------------
    private void elsePart(int pc)
    {
        int label2;
        int dumbyVal = 0;
        if (currentToken.kind == ELSE)
        {
            cg.emit(JA);
            cg.emit(dumbyVal);
            consume(ELSE);
            cg.changeCode(pc, cg.getSize());
            label2 = cg.getSize()-1;

            statement();
            cg.changeCode(label2, cg.getSize());
            //cg.emitLabel(label2);
        }
        else
            cg.changeCode(pc, cg.getSize()); 
    }
    //-----------------------------------------
    private void doWhileStatement()
    {
        String label;
        int start = cg.getSize();
        consume(DO);
        //label = cg.getLabel();
        //cg.emitLabel(label);
        statement();
        consume(WHILE);
        consume(LEFTPAREN);
        expr();
        cg.emit(JNZ);
        cg.emit(start);
        //cg.emitInstruction("jnz", label);
        consume(RIGHTPAREN);
        consume(SEMICOLON);
    }
    //-----------------------------------------
    private void expr()
    {
        term();
        termList();
    }
    //-----------------------------------------
    private void termList()
    {
        switch(currentToken.kind)
        {
            case PLUS:
            consume(PLUS);
            term();
            cg.emit(PLUS);
            termList();
            break;
            case MINUS:
            consume(MINUS);
            term();
            cg.emit(MINUS);
            case RIGHTPAREN:
            case SEMICOLON:
            ;
            break;
            default:
            throw 
            genEx("Expecting \"+\", \"-\", \")\", or \";\"");
        }
    }
    //-----------------------------------------
    private void term()
    {
        factor();
        factorList();
    }
    //-----------------------------------------
    private void factorList()
    {
        switch(currentToken.kind)
        {
            case TIMES:
            consume(TIMES);
            factor();
            cg.emit(TIMES);
            factorList();
            break;
            case DIVIDE:  
            consume(DIVIDE);
            factor();
            cg.emit(DIVIDE);
            factorList();
            break;
            case PLUS:
            case MINUS:
            case RIGHTPAREN:
            case SEMICOLON:
            ;
            break;
            default:
            throw genEx("Expecting op, \")\", or \";\"");
        }
    }
    //-----------------------------------------
    private void factor()
    {  
        Token t;

        switch(currentToken.kind)
        {
            case UNSIGNED:
            t = currentToken;
            if (t.image.length() > 5 ||
            Integer.parseInt(t.image) > 32767)
                System.out.println("Expecting integer (-32768 to 32767)");
            consume(UNSIGNED);
            cg.emit(PUSHCONSTANT);
            cg.emit(Integer.parseInt(t.image));
            break;
            case ID:
            t = currentToken;
            consume(ID);
            int value = st.enter(t.image);
            cg.emit(PUSH);
            cg.emit(value);

            break;
            case LEFTPAREN:
            consume(LEFTPAREN);
            expr();
            consume(RIGHTPAREN);
            break;
            case PLUS:
            consume(PLUS);
            factor();
            break;
            case MINUS:
            consume(MINUS);
            switch(currentToken.kind)
            {
                case UNSIGNED:
                t = currentToken;
                if (t.image.length() > 5 ||
                Integer.parseInt(t.image) > 32768)
                    System.out.println("Expecting integer (-32768 to 32767)");
                consume(UNSIGNED);
                cg.emit(PUSHCONSTANT);
                cg.emit(Integer.parseInt(t.image));
                cg.emit(NEG);
                break;
                case ID:
                t = currentToken;
                consume(ID);
                int value1 = st.enter(t.image);
                cg.emit(PUSH);
                cg.emit(value1);
                cg.emit(NEG);
                break;
                case LEFTPAREN:
                consume(LEFTPAREN);
                expr();
                consume(RIGHTPAREN);
                cg.emit(NEG);
                break;
                case PLUS:
                do
                {
                    consume(PLUS);
                }
                while (currentToken.kind == PLUS);
                if (currentToken.kind == MINUS)
                {
                    consume(MINUS);
                    factor();
                }
                else
                {
                    factor();
                    cg.emit(NEG);
                }
                break;
                case MINUS:
                consume(MINUS);
                factor();
                break;
            }
            break;
            default:
            throw genEx("Expecting factor");
        }
    }
}                               // end of CI4Parser class
//======================================================
class CI4CodeGen implements CI4Constants
{
    private PrintWriter outFile;
    private CI4SymTab st;
    private int labelNumber;

    private ArrayList<Integer> scode;
    private Stack<Integer> s;
    int[] vtab;
    private int pc;
    private int opcode;
    private Scanner sin;
    private ArrayList<String> stringList;
    //-----------------------------------------
    public CI4CodeGen(PrintWriter outFile, CI4SymTab st)
    {
        scode= new ArrayList<Integer>();
        s = new Stack<Integer>();
        pc = 0;
        sin = new Scanner(System.in);
        stringList = new ArrayList<String>();
    }
    //-----------------------------------------
    public int getSize()
    {
        return scode.size();
    }

    public void changeCode(int loc, int newVal)
    {
        scode.set(loc, newVal);
    }

    public void emit(int inst)
    {
        scode.add(inst);
    }
    //-----------------------------------------
    public void makevtab(int size)
    {
        vtab = new int[size];  
    }

    public int saveString(String value)
    {
        int index = stringList.indexOf(value);

        // if s is not in symbol, then add it 
        if (index < 0){
            stringList.add(value);

        }
        index = stringList.indexOf(value);
        return index;
    }

    public void interpret()
    {
        boolean doAgain = true;
        int right;

        do
        {
            opcode = scode.get(pc++);
            //System.out.println(opcode);
            switch(opcode)
            {
                case PRINTLN:
                System.out.println(s.pop());
                break;
                case PRINT:
                System.out.print(s.pop());
                break;
                case READINT://making this into a try/catch
                //add if (right> 5 || right > 32768)
                //System.out.println("Expecting integer (-32768 to 32767)");
                boolean value;
                right = 0;
                do{
                    try{
                        String testIn = sin.next();
                        right = Integer.parseInt(testIn);
                        value = true;
                    }
                    catch(NumberFormatException e){
                        value = false;
                        System.out.println("No. Try that again.");
                    }
                }while (!value);
                vtab[scode.get(pc++)] = right;
                break;
                case ASSIGN:
                vtab[scode.get(pc++)] = s.pop();
                break;
                case PLUS:
                right = s.pop();
                s.push(s.pop() + right);
                break;
                case MINUS:
                right = s.pop();
                s.push(s.pop()- right);
                break;
                case TIMES:
                right = s.pop();
                s.push(s.pop() * right);
                break;
                case DIVIDE:
                right = s.pop();
                s.push(s.pop()/right);
                break;
                case DUPE:
                right = s.pop();
                s.push(right);
                s.push(right);
                break;
                case PUSHCONSTANT:
                s.push(scode.get(pc++));
                break;
                case PUSH:
                s.push(vtab[scode.get(pc++)]);
                break;
                case NEWLINE:
                System.out.println();
                break;
                case PRINTSTRING:
                System.out.print(stringList.get(scode.get(pc++)));
                break;
                case NEG:
                right = s.pop();
                s.push(right *-1);
                break;
                case JA:
                pc = scode.get(pc++);
                break;
                case JNZ:
                if(s.pop() != 0)
                    pc = scode.get(pc++);
                else
                    pc++;
                break;
                case JZ:
                if(s.pop() == 0)
                    pc = scode.get(pc++);
                else
                    pc++;
                break;
                case HALT:
                doAgain = false;
                break;
                default:
                doAgain = true;
                break;
            }
        } while(doAgain);

    }
}                        // end of CI4CodeGen class

