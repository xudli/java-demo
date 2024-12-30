package com.github.xudli.raft;

public class LogEntry {
    private final int term;
    private final String command;
    private final int index;
    
    public LogEntry(int term, String command, int index) {
        this.term = term;
        this.command = command;
        this.index = index;
    }
    
    public int getTerm() {
        return term;
    }
    
    public String getCommand() {
        return command;
    }
    
    public int getIndex() {
        return index;
    }
} 