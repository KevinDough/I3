// Hand-written S1 compiler
import java.io.*;
import java.util.*;
//======================================================
class I1
{  
  public static void main(String[] args) throws 
                                             IOException
  {
    System.out.println("I1 compiler written by Kevin Dougherty");

    if (args.length != 1)
    {
      System.err.println("Wrong number cmd line args");  
      System.exit(1);
    }

    // set to true to debug token manager
    boolean debug = false;

    // build the input and output file names
    String inFileName = args[0] + ".s";
    //String outFileName = args[0] + ".a";

    // construct file objects
    Scanner inFile = new Scanner(new File(inFileName));
    //PrintWriter outFile = new PrintWriter(outFileName);

    // identify compiler/author in the output file
    //outFile.println("; from I1 compiler written by Kevin Dougherty");

    // construct objects that make up compiler
    I1SymTab st = new I1SymTab();
    I1TokenMgr tm =  new I1TokenMgr(inFile, debug);
    
    I1Parser parser = new I1Parser(st, tm);

    // parse and translate
    try
    {
      parser.parse();
    }      
    catch (RuntimeException e) 
    {
      System.err.println(e.getMessage());
      //outFile.println(e.getMessage());
      //outFile.close();
      System.exit(1);
    }

    //outFile.close();
  }
}                                           // end of I1
//======================================================
interface I1Constants
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
    "<ERROR>"
  };
}                                  // end of I1Constants
//======================================================
class I1SymTab
{
  private ArrayList<String> symbol;
  private ArrayList<Integer> value;
  //-----------------------------------------
  public I1SymTab()
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
        value.add(index, v);
    }
}                                     // end of I1SymTab
//======================================================
class I1TokenMgr implements I1Constants
{
  private Scanner inFile;          
  //private PrintWriter outFile;
  private boolean debug;
  private char currentChar;
  private int currentColumnNumber;
  private int currentLineNumber;
  private String inputLine;    // holds 1 line of input
  private Token token;         // holds 1 token
  private StringBuffer buffer; // token image built here
  //-----------------------------------------
  public I1TokenMgr(Scanner inFile, boolean debug)
  {
    this.inFile = inFile;
    //this.outFile = outFile;
    this.debug = debug;
    currentChar = '\n';        //  '\n' triggers read
    currentLineNumber = 0;
    buffer = new StringBuffer();
  }
  //-----------------------------------------
  public Token getNextToken()
  {
    // skip whitespace
    while (Character.isWhitespace(currentChar))
      getNextChar();

    // construct token to be returned to parser
    token = new Token();   
    token.next = null;

    // save start-of-token position
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
      // save buffer as String in token.image
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
      // save buffer as String in token.image
      token.image = buffer.toString();

      // check if keyword
      if (token.image.equals("println"))
        token.kind = PRINTLN;
      else  // not a keyword so kind is ID
        token.kind = ID;
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
        default:
          token.kind = ERROR;
          break;                               
      }

      // save currentChar as String in token.image
      token.image = Character.toString(currentChar);

      // save end-of-token position
      token.endLine = currentLineNumber;
      token.endColumn = currentColumnNumber;

      getNextChar();  // read beyond end of token
    }

    // token trace appears as comments in output file
    /*
    if (debug)
      outFile.printf(
        "; kd=%3d bL=%3d bC=%3d eL=%3d eC=%3d im=%s%n",
        token.kind, token.beginLine, token.beginColumn, 
        token.endLine, token.endColumn, token.image);
    */

    return token;     // return token to parser
  }     
  //-----------------------------------------
  private void getNextChar()
  {
    if (currentChar == EOF)
      return;

    if (currentChar == '\n')        // need next line?
    {
      if (inFile.hasNextLine())     // any lines left?
      {
        inputLine = inFile.nextLine();  // get next line
        // output source line as comment
        //outFile.println("; " + inputLine);
        inputLine = inputLine + "\n";   // mark line end
        currentColumnNumber = 0;
        currentLineNumber++;   
      }                                
      else  // at end of file
      {
         currentChar = EOF;
         return;
      }
    }

    // get next char from inputLine
    currentChar = 
                inputLine.charAt(currentColumnNumber++);

    // in S2, test for single-line comment goes here
  }
}                                   // end of I1TokenMgr
//======================================================
class I1Parser implements I1Constants
{
  private I1SymTab st;
  private I1TokenMgr tm;
  //private I1CodeGen cg;
  private Token currentToken;
  private Token previousToken; 
  private Stack<Integer> s; //= new Stack<Integer>();
  //-----------------------------------------
  public I1Parser(I1SymTab st, I1TokenMgr tm) 
                                        
  {
    this.st = st;
    this.tm = tm;
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
      currentToken.beginLine + ", column " + 
      currentToken.beginColumn + "." +
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
    if (currentToken.next != null)
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
    if (currentToken.kind == expected)
      advance();
    else
      throw genEx("Expecting " + tokenImage[expected]);
  }
  //-----------------------------------------
  public void parse()
  {
    program();   // program is start symbol for grammar
  }
  //-----------------------------------------
  private void program()
  {
    statementList();
   
    if (currentToken.kind != EOF)  //garbage at end?
      throw genEx("Expecting <EOF>");
  }
  //-----------------------------------------
  private void statementList()
  {
    switch(currentToken.kind)
    {
      case ID:
      case PRINTLN:
        statement();
        statementList();
        break;
      case EOF:
        ;
        break;
      default:
        throw genEx("Expecting statement or <EOF>");
    }
  }
  //-----------------------------------------
  private void statement()
  {
    switch(currentToken.kind)
    {
      case ID: 
        assignmentStatement(); 
        break;
      case PRINTLN:    
        printlnStatement(); 
        break;
      default:         
        throw genEx("Expecting statement");
    }
  }
  //-----------------------------------------
  private void assignmentStatement()
  {
    Token t;
    int left, expVal;

    t = currentToken;
    consume(ID);
    left = st.enter(t.image);
   // System.out.println("Assignment statement");
    consume(ASSIGN);
    expr();
    st.setValue(left, s.pop());
    consume(SEMICOLON);
  }
  //-----------------------------------------
  private void printlnStatement()
  {
    consume(PRINTLN);
    consume(LEFTPAREN);
   // System.out.println("about to print");
    expr();
    System.out.println(s.pop());
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
    int right;
    switch(currentToken.kind)
    {
      case PLUS:
        consume(PLUS);
        //System.out.println("Add statement");
        term();
        right = s.pop();
        s.push(s.pop()+right);
        termList();
        break;
      case RIGHTPAREN:
      case SEMICOLON:
        ;
        break;
      default:
        throw genEx("Expecting \"+\", \")\", or \";\"");
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
        int right = s.pop();
        s.push(right *s.pop());
        factorList();
        break;
      case PLUS:
      case RIGHTPAREN:
      case SEMICOLON:
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
        consume(UNSIGNED);
        s.push(Integer.parseInt(t.image));
        //System.out.println("unsigned");
        break;
      case PLUS:
        consume(PLUS);
        t = currentToken;
        consume(UNSIGNED);
        s.push(Integer.parseInt(t.image));
        break;
      case MINUS:
        consume(MINUS);
        t = currentToken;
        consume(UNSIGNED);
        s.push(-1 *(Integer.parseInt(t.image)));
        break;
      case ID:
        t = currentToken;
        consume(ID);
        int butt = st.enter(t.image);
        
        s.push(st.getValue(butt));
        break;
      case LEFTPAREN:
        consume(LEFTPAREN);
        expr();
        consume(RIGHTPAREN);
        break;
      default:
        throw genEx("Expecting factor");
    }
  }
}                                     // end of I1Parser

