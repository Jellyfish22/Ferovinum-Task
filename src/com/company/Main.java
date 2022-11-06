package com.company;

import java.io.FileDescriptor;

public class Main {

    public static void main(String[] args) throws Exception {
        PostFixEvaluation postFixEvaluation = new PostFixEvaluation();
        postFixEvaluation.evaluate(args[0]);
        postFixEvaluation.writeToFile(FileDescriptor.out);
    }
}