package com.craftinginterepter.sky;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class Sky{
    static boolean hadError = false;
    public static void main(String[] args) throws IOException{
        if(args.length > 1){
            System.out.println("Usage: jsky [script]");
            System.exit(64);
        } else if (args.length == 1){
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
        
    }

    // running in cmd or in a terminal
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;){
            System.out.println(">> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for(Token token: tokens){
            System.out.println(token);
        }
    }

    // Error handling
    static void error(int line, String messege){
        report(line,"",messege);
    }

    private static void report(int line, String where, String messege){
        System.err.println("[ERROR] line " + line + where + ": " + messege);
        hadError = true;
    }
}